package com.fis.epm.task;

import com.fis.epm.entity.EpmUserPG;
import com.fis.epm.repo.ApParamRedisRepo;
import com.fis.epm.service.PGService;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.epm.utils.ResttemplateBean;
import com.fis.fw.common.utils.ValidationUtil;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.epm.models.ApParamRedisModel;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.epm.models.ObjSendResultEPMToMBF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskCallPG {
    private static final Logger logger = LoggerFactory.getLogger(TaskCallMyMBFResult.class);
    private static final int MAX_SIZE = 100;
    private static final long MAX_TIME_OUT = 5 * 1000; // 5 seconds
    private static final List<EpmTransactionModel> listTran = new ArrayList<>();
    private long lastUpdateTime = System.currentTimeMillis();

    @Autowired
    private PGService pgService;

    @Autowired
    private ApParamRedisRepo apParamRedisRepo;

    @Value("${com.fis.pg.retry.issue}")
    private int maxNumberRetry;

    @Scheduled(fixedDelay = 10)
    public void process() {
        EpmTransactionModel tran = EPMQueueManager.QUEUE_EXPORT_ORDER.dequeueWait(1);

        if (tran != null) {
            listTran.add(tran);
        }

        if (listTran.size() >= MAX_SIZE || System.currentTimeMillis() - lastUpdateTime > MAX_TIME_OUT) {
            if (!listTran.isEmpty()) {
                for (EpmTransactionModel obj : listTran) {
                    try {
                        String serviceCode = "";
                        String typeChannel = StringUtils.nvl(obj.getTypeProduct(),"");
                        if(!ValidationUtil.isNullOrEmpty(typeChannel)){
                            typeChannel = typeChannel + "_";
                        }


                        ApParamRedisModel apParamRedisModel = apParamRedisRepo.findByParTypeAndParName("SERVICE_CODE_EPM_BY_CHANNEL", typeChannel + obj.getPaymentChanelCode() + "_" + obj.getObjectType());
                        logger.info("SERVICE_CODE_EPM_BY_CHANNEL: " + typeChannel + obj.getPaymentChanelCode() + "_" + obj.getObjectType());
                        if(apParamRedisModel == null){
                            List<ApParamRedisModel> lstApParam = apParamRedisRepo.findByParType("SERVICE_CODE_EPM_BY_CHANNEL");
                            logger.info("lstApParam" + lstApParam);
                            throw new AppException("Khong tim thay thong tin cua service Code");
                        }
                        serviceCode = apParamRedisModel.getParValue();
                        EpmUserPG user = EPMBaseCommon.mapUserByService.get(serviceCode);
                        if(user == null){
                            throw new AppException("Khong tim thay thong tin user cua service Code : " + serviceCode);
                        }
                        String userName = user.getUserName();
                        String password = user.getPassword();
                        pgService.autoExecutePG(obj, userName , password);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        int countRetry = obj.getCountRetry() + 1;
                        if (countRetry < maxNumberRetry) {
                            EPMQueueManager.QUEUE_EXPORT_ORDER.enqueueNotify(obj);
                        }
                    }
                }
                listTran.clear();
            }
            lastUpdateTime = System.currentTimeMillis();
        }
    }
}
