package com.fis.epm.service.impl;

import com.fis.epm.dao.EPMAppDao;
import com.fis.epm.dao.EPMTransactionDao;
import com.fis.epm.entity.EpmTransaction;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.prop.EPMMessageCode;
import com.fis.epm.repo.EpmTransactionRepo;
import com.fis.epm.repo.IsdnInfoRedisRepo;
import com.fis.epm.repo.QueryDRRedisRepo;
import com.fis.epm.service.MBMoneyService;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.epm.utils.MobiFoneMoneyUtils;
import com.fis.epm.utils.Utils;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.epm.models.IsdnInfoRedisModel;
import com.fis.pg.gw.server.models.ResponseModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Optional;


@Service
public class MBMoneyServiceImpl implements MBMoneyService {
    private static final Logger log = LoggerFactory.getLogger(MBMoneyServiceImpl.class);
    Gson gson = new Gson();

    @Value("${com.fis.mobifone.money.key.3DES}")
    private String key3DESMBFMoney;

    @Autowired
    private EpmTransactionRepo epmTransactionRepo;

    @Autowired
    private EPMAppDao epmAppDao;

    @Autowired
    private EPMTransactionDao epmTransactionDao;

    @Autowired
    private QueryDRRedisRepo queryDrRedisRepo;

    @Autowired
    private IsdnInfoRedisRepo isdnInfoRedisRepo;

    @Override
    public ResponseModel callback(String url, String ipRequest, Map request) {
        LogApiResult logApiResult = Utils.initLog("POST", url, null,
                Tools.convertModeltoJSON(request), null);
        ResponseModel res = new ResponseModel();
        try {
            String data = request.get("data").toString();
            String response = MobiFoneMoneyUtils.decrypt3DES(key3DESMBFMoney, data);
            log.info("Mobifone Money CallBack Result: {} ", response);
            JsonObject resultObject = gson.fromJson(response.toString(), JsonObject.class);
            String transactionIdMBMoney = resultObject.get("transactionId").toString().trim().replaceAll("\"","");
            log.info("transactionIdMBMoney : '{}'", transactionIdMBMoney);
            String transactionId = transactionIdMBMoney.substring(8);
            log.info("transactionId : {}", transactionId);
            EpmTransaction transaction = epmTransactionRepo.findByTransactionID(transactionId);
            if (transaction == null) {
                log.error("=================>Khong tim thay du lieu trong bang tran EpmTransaction");
                throw new AppException(EPMMessageCode.API_EXCEPTION_CODE);
            }
            logApiResult.setTranId(transactionId);
            if (transaction.getPayStatus() != null && !EmpStatusConstain.START.equals(transaction.getPayStatus())) {
                log.error("=================>Đa co giao dich trong EpmTransaction");
                throw new AppException(EPMMessageCode.API_EXCEPTION_CODE);
            }
            transaction.setPayStatus(EmpStatusConstain.FAILED);
            transaction.setIssueStatus(EmpStatusConstain.FAILED);
            EpmTransactionModel empTransactionModel = new EpmTransactionModel();
            empTransactionModel = transaction.setEpmTransactionModel(transaction);
            Optional<IsdnInfoRedisModel> isdnInfo = isdnInfoRedisRepo.findById(empTransactionModel.getReference());
            if (isdnInfo.isPresent() && isdnInfo.get() != null) {
                empTransactionModel.setCustCode(isdnInfo.get().getCustCode());
            }
            if ("1".equals(StringUtils.nvl(resultObject.get("status"), ""))) {
                empTransactionModel.setPayStatus(EmpStatusConstain.SUCC);
                empTransactionModel.setIssueStatus(EmpStatusConstain.PENDING);
                empTransactionModel.setCardNumber(StringUtils.nvl(resultObject.get("accountNumber"), ""));
//            empTransactionModel.setCardHolder();
                epmAppDao.insertMerChantLog(Utils.genMerchantLog(empTransactionModel));
                epmTransactionDao.updatePaymentEPM(empTransactionModel);
                queryDrRedisRepo.deleteById(transaction.getTransactionId());
                EPMQueueManager.QUEUE_EXPORT_ORDER.enqueueNotify(empTransactionModel);
                res.setStatus(EPMMessageCode.API_SUCCESSED_CODE);
                res.setMessage("Thành công");
                logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
                return res;
            }
            epmAppDao.insertMerChantLog(Utils.genMerchantLog(empTransactionModel));
            epmTransactionDao.updatePaymentEPM(empTransactionModel);
            queryDrRedisRepo.deleteById(transaction.getTransactionId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            res = Tools.buildResponseModel(e);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            logApiResult.setEndTime(new Date());
            long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(ipRequest);
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }
}
