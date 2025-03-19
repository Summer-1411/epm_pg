package com.fis.epm.service.impl;

import com.fis.epm.dao.EPMAppDao;
import com.fis.epm.dao.EPMTransactionDao;
import com.fis.epm.entity.EpmTransaction;
import com.fis.epm.entity.EpmUserPG;
import com.fis.epm.models.PGExecuteBodyModel;
import com.fis.epm.models.PGExecuteRequestModel;
import com.fis.epm.models.TokenPG;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.repo.ApParamRedisRepo;
import com.fis.epm.repo.EpmTransactionRepo;
import com.fis.epm.service.PGService;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.epm.utils.ResttemplateBean;
import com.fis.fw.common.exceptions.AppException;
import com.fis.fw.common.utils.AlertSystemUtil;
import com.fis.fw.common.utils.DateUtil;
import com.fis.fw.common.utils.StringUtil;
import com.fis.fw.common.utils.ValidationUtil;
import com.fis.pg.common.utils.ATMUtil;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.ApParamRedisModel;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.gw.server.message.MessagePackage;
import com.fis.pg.gw.server.message.VMSIso8583MessageV2;
import com.fis.pg.gw.server.models.EpmIssueLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class PGServiceImpl implements PGService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Value("${com.fis.pg.ip}")
    private String uri;
    @Value("${com.fis.pg.path_Login}")
    private String pathLogin;
    @Value("${com.fis.pg.path_Execute}")
    private String path_Execute;
    @Value("${com.fis.pg.processCodePre}")
    private String processCodePre;
    @Value("${com.fis.pg.processCodePos}")
    private String processCodePos;
    @Value("${com.fis.pg.apiTimeOut}")
    private Integer apiTimeOut;

    @Value("${com.fis.pg.url-alert}")
    private String urlAlert;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${com.fis.ip.server}")
    private String ipServer;

    @Value("${com.fis.pg.timeTokenExpire}")
    private Long timeTokenExpire;

    @Autowired
    private EPMAppDao epmAppDao;

    @Value("${com.fis.epm.mobifiber.type.product}")
    private String typeProductMobifiber;

    @Autowired
    private EPMTransactionDao epmTransactionDao;

    @Autowired
    private ApParamRedisRepo apParamRedisRepo;

    @Autowired
    private EpmTransactionRepo epmTransactionRepo;

    @Autowired
    private ResttemplateBean rt;

    private static final Long timeLoginPG = null;

    @Override
    public String loginPG(String userName, String password) {
        String strToken = null;
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> mapBody = new LinkedMultiValueMap<>();
        mapBody.add("userName", userName);
        mapBody.add("password", password);
        Map response = (Map) rt.handleHttpRequest(uri + pathLogin, apiTimeOut, HttpMethod.POST, header, mapBody, Map.class);
        if (response != null) {
            Map payload = (Map) response.get("payload");
            if (payload != null) {
                Map token = (Map) payload.get("token");
                if (token != null) {
                    strToken = (String) token.get("tokenKey");
                    Long expireTime = System.currentTimeMillis() + timeTokenExpire;
                    TokenPG tokenPG = new TokenPG();
                    tokenPG.setExpireDate(expireTime);
                    tokenPG.setToken(strToken);
                    EPMBaseCommon.mapTokenPGByUser.put(userName, tokenPG);

                }
            }
        }
        logger.info("==========START CALL API LOGIN PG REST at " + System.currentTimeMillis() + " ms==========");
        return strToken;
    }

    @Override
    public void autoExecutePG(EpmTransactionModel tran, String user, String pass) throws Exception {
        String responseCode = execute(tran, user, pass);
        if (ATMUtil.RES_BANK_SUCCESS.equals(responseCode)) {
            tran.setIssueStatus(EmpStatusConstain.SUCC);
        } else {
            tran.setIssueStatus(EmpStatusConstain.FAILED);
        }
        epmAppDao.insertIssueLog(setDataIssueLog(tran));
        epmTransactionDao.updatePaymentPG(tran);
    }

    @Override
    public void manualExecutePG(List<String> lsyTranId) throws Exception {
        List<EpmTransaction> lstTran = epmTransactionRepo.findListTransactionId(lsyTranId);
        if (lstTran != null && !lstTran.isEmpty()) {
            for (EpmTransaction tran : lstTran) {
                EpmTransactionModel tranModel = new EpmTransactionModel();
                tranModel = tran.setEpmTransactionModel(tran);
                String serviceCode = "";
                String typeChannel = StringUtils.nvl(tran.getTypeProduct(),"");
                if(!ValidationUtil.isNullOrEmpty(typeChannel)){
                    typeChannel = typeChannel + "_";
                }
                ApParamRedisModel apParamRedisModel = apParamRedisRepo.findByParTypeAndParName("SERVICE_CODE_EPM_BY_CHANNEL", typeChannel + tranModel.getPaymentChanelCode() + "_" + tranModel.getObjectType());
                serviceCode = apParamRedisModel.getParValue();
                EpmUserPG user = EPMBaseCommon.mapUserByService.get(serviceCode);
                if (user == null) {
                    logger.info("Khong tim thay thong tin user cua service Code : " + serviceCode);
                    continue;
                }
                String userName = user.getUserName();
                String password = user.getPassword();
                String responseCode = execute(tranModel, userName, password);
                if (ATMUtil.RES_BANK_SUCCESS.equals(responseCode)) {
                    tran.setIssueStatus(EmpStatusConstain.SUCC);
                } else {
                    tran.setIssueStatus(EmpStatusConstain.FAILED);
                }
                epmAppDao.insertIssueLog(setDataIssueLog(tranModel));
                epmTransactionDao.updatePaymentPG(tranModel);
            }
        }
    }

    private EpmIssueLog setDataIssueLog(EpmTransactionModel data) {
        EpmIssueLog issue = new EpmIssueLog();
        issue.setTransactionId(data.getTransactionId());
        issue.setUserName(data.getUserName());
        issue.setBillCycleId(data.getBillCycleId());
        issue.setIssueStatus(data.getIssueStatus());
        issue.setAmount(data.getIssueAmount());
        issue.setDiscountAmount(data.getDiscountAmount());
        issue.setPromAmount(data.getPromAmount());
        issue.setBankCode(data.getBankCode());
        issue.setObjectType(data.getObjectType());
        issue.setCustCode(data.getCustCode());
        issue.setRequestDate(data.getStaDateTime());
        issue.setResponseDate(new Date());
        issue.setCenCode(data.getCenCode());
        issue.setReference(data.getReference());
        return issue;
    }


    private String execute(EpmTransactionModel tran, String user, String pass) throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("==========START CALL API EXECUTE REST at " + startTime + " ms==========");
        TokenPG tokenPG = EPMBaseCommon.mapTokenPGByUser.get(user);
        String token = "";
        if (tokenPG == null || tokenPG.getExpireDate() == null || tokenPG.getExpireDate() < startTime) {
            token = loginPG(user, pass);
        } else {
            token = tokenPG.getToken();
        }
        if(token == null){
            AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : Not login PG Core with user :" +  user);
            return "";
        }
        PGExecuteRequestModel obj = convertToObjectRest(tran, token);

        String strResponseCode = "";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
       // HttpEntity request = new HttpEntity<>(obj, header);

        try {
            Map response = (Map) rt.handleHttpRequest(uri + path_Execute, apiTimeOut, HttpMethod.POST, header, obj, Map.class);
            if (response != null) {
                logger.info("==========RESPONSE CALL API EXECUTE REST in " + Tools.convertModeltoJSON(response));
                String payload = (String) response.get("payload");
                MessagePackage msg = new VMSIso8583MessageV2();
                msg.unpackXML(payload);
                if ("05".equals(msg.getRespondCode())) {
                    autoExecutePG(tran, user, pass);
                }
                strResponseCode = msg.getRespondCode();
                return strResponseCode;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            logger.info("==========DONE CALL API EXECUTE REST in " + (System.currentTimeMillis() - startTime) + " ms==========");
        }
        return strResponseCode;
    }

    private PGExecuteRequestModel convertToObjectRest(EpmTransactionModel tran, String token) throws Exception {
        PGExecuteRequestModel pgReturn = new PGExecuteRequestModel();
        List<PGExecuteBodyModel> body = new ArrayList<>();
        String serviceCode = "";
        String field61Value = "";
        if (tran.getObjectType() == null || "".equals(tran.getObjectType())) {
            throw new AppException("Không thể xác định loại thuê bao");
        }
        String typeChannel = StringUtils.nvl(tran.getTypeProduct(),"");
        if(!ValidationUtil.isNullOrEmpty(typeChannel)){
            typeChannel = typeChannel + "_";
        }
        String processCode = "";
        ApParamRedisModel apParamProcess = apParamRedisRepo.findByParTypeAndParName("PROCESS_CODE_EPM", typeChannel + tran.getObjectType());
        logger.info("Check Convert to Object typeChannel: " + typeChannel + ", ObjectType: " + tran.getObjectType());
        if (apParamProcess == null) {
            throw new AppException("Chưa cấu hình processCode");
        }
        processCode = apParamProcess.getParValue();
        if (EmpStatusConstain.OT_PRE.equals(tran.getObjectType())) {
            PGExecuteBodyModel field0 = new PGExecuteBodyModel("0", ATMUtil.TYPE_PAYMENT_AIRTIME);
            body.add(field0);
            PGExecuteBodyModel field3 = new PGExecuteBodyModel("3", processCode);
            body.add(field3);
            //serviceCode = ATMUtil.mapServiceCodeEPM.get(tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            ApParamRedisModel apParamRedisModel = apParamRedisRepo.findByParTypeAndParName("SERVICE_CODE_EPM_BY_CHANNEL", typeChannel + tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            serviceCode = apParamRedisModel.getParValue();
            field61Value = tran.getReference() + "|" + (int) tran.getIssueAmount()
                    + "|" + (int) tran.getIssueAmount();
            PGExecuteBodyModel field94 = new PGExecuteBodyModel("94", serviceCode);
            body.add(field94);
            PGExecuteBodyModel field61 = new PGExecuteBodyModel("61", field61Value);
            body.add(field61);
        }
        if (EmpStatusConstain.OT_POS.equals(tran.getObjectType())) {
            PGExecuteBodyModel field0 = new PGExecuteBodyModel("0", ATMUtil.TYPE_PAYMENT);
            body.add(field0);
            PGExecuteBodyModel field3 = new PGExecuteBodyModel("3", processCode);
            body.add(field3);
            //serviceCode = ATMUtil.mapServiceCodeEPM.get(tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            ApParamRedisModel apParamRedisModel = apParamRedisRepo.findByParTypeAndParName("SERVICE_CODE_EPM_BY_CHANNEL", typeChannel + tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            serviceCode = apParamRedisModel.getParValue();
            field61Value = tran.getReference() + "|" + StringUtil.nvl(tran.getCustCode(), "")
                    + "|" + (int) tran.getIssueAmount();
            logger.info("Discount Amount of Transactions :" + tran.getTransactionId() + " is " + tran.getDiscountAmount());
            logger.info("Amount of Transactions :" + tran.getTransactionId() + " is " + tran.getAmount());
            PGExecuteBodyModel field94 = new PGExecuteBodyModel("94", serviceCode);
            body.add(field94);
            PGExecuteBodyModel field61 = new PGExecuteBodyModel("61", field61Value);
            body.add(field61);
            if (tran.getDiscountAmount() > 0) {
                PGExecuteBodyModel field78 = new PGExecuteBodyModel("78", String.valueOf((int)tran.getDiscountAmount()));
                body.add(field78);
                PGExecuteBodyModel field79 = new PGExecuteBodyModel("79", "0");
                body.add(field79);
            }
        }
        if (EPMApiConstant.CREATE_PACKAGE.equals(tran.getObjectType())) {
            EpmTransaction epmTransaction = epmTransactionRepo.findByTransactionID(tran.getTransactionId());
            PGExecuteBodyModel field0 = new PGExecuteBodyModel("0", ATMUtil.TYPE_PAYMENT);
            body.add(field0);
            PGExecuteBodyModel field3 = new PGExecuteBodyModel("3", processCode);
            body.add(field3);
//            serviceCode = ATMUtil.mapServiceCodeEPM.get(tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            ApParamRedisModel apParamRedisModel = apParamRedisRepo.findByParTypeAndParName("SERVICE_CODE_EPM_BY_CHANNEL", typeChannel + tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            serviceCode = apParamRedisModel.getParValue();
            field61Value = tran.getReference() + "|" + epmTransaction.getPackageCode()
                    + "|" + (int) tran.getIssueAmount();
            logger.info("Check transaction call pg-service: " + tran.getReference() + "|" + epmTransaction.getPackageCode()
                    + "|" + (int) tran.getIssueAmount());
            PGExecuteBodyModel field94 = new PGExecuteBodyModel("94", serviceCode);
            body.add(field94);
            PGExecuteBodyModel field61 = new PGExecuteBodyModel("61", field61Value);
            body.add(field61);
        }
        if(!ValidationUtil.isNullOrEmpty(tran.getTypeProduct()) && typeProductMobifiber.equals(tran.getTypeProduct())){
            body = new ArrayList<>();
            PGExecuteBodyModel field0 = new PGExecuteBodyModel("0", ATMUtil.TYPE_PAYMENT);
            body.add(field0);
            PGExecuteBodyModel field3 = new PGExecuteBodyModel("3", processCode);
            body.add(field3);
            //serviceCode = ATMUtil.mapServiceCodeEPM.get(tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            ApParamRedisModel apParamRedisModel = apParamRedisRepo.findByParTypeAndParName("SERVICE_CODE_EPM_BY_CHANNEL", typeChannel + tran.getPaymentChanelCode() + "_" + tran.getObjectType());
            serviceCode = apParamRedisModel.getParValue();
            field61Value = tran.getCustCode() + "|" + tran.getReference() +"|" + (int) tran.getIssueAmount();
            PGExecuteBodyModel field94 = new PGExecuteBodyModel("94", serviceCode);
            body.add(field94);
            PGExecuteBodyModel field61 = new PGExecuteBodyModel("61", field61Value);
            body.add(field61);
            if (tran.getDiscountAmount() > 0) {
                PGExecuteBodyModel field78 = new PGExecuteBodyModel("78", String.valueOf((int)tran.getDiscountAmount()));
                body.add(field78);
                PGExecuteBodyModel field79 = new PGExecuteBodyModel("79", "0");
                body.add(field79);
            }
        }
        PGExecuteBodyModel field7 = new PGExecuteBodyModel("7", DateUtil.dateToString(new java.util.Date(), "MMddhhmmss"));
        body.add(field7);
        PGExecuteBodyModel field42 = new PGExecuteBodyModel("42", tran.getTransactionId());
        body.add(field42);
        PGExecuteBodyModel field73 = new PGExecuteBodyModel("73", DateUtil.dateToString(new java.util.Date(), "yyMMdd"));
        body.add(field73);
        logger.info("Payload call pg-service: " + body.toString());
        pgReturn.setBody(body);
        pgReturn.setToken(token);
        return pgReturn;
    }

}
