package com.fis.epm.task;

import com.fis.epm.entity.LogApiResult;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.epm.utils.ResttemplateBean;
import com.fis.pg.epm.models.ObjSendResultEPMToMBF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TaskCallMyMBFResult {
    private static final Logger logger = LoggerFactory.getLogger(TaskCallMyMBFResult.class);
    private static final int MAX_SIZE = 100;
    private static final long MAX_TIME_OUT = 5 * 1000; // 5 seconds
    private static final List<ObjSendResultEPMToMBF> listAPI = new ArrayList<>();
    private long lastUpdateTime = System.currentTimeMillis();

    @Autowired
    private ResttemplateBean resttemplateBean;

    @Scheduled(fixedDelay = 50)
    public void process() {
        ObjSendResultEPMToMBF api = EPMQueueManager.QUEUE_SEC_RESULT_EPM_TO_MBF.dequeueWait(1);
        if(api != null){
            listAPI.add(api);
        }

        if (listAPI.size() >= MAX_SIZE || System.currentTimeMillis() - lastUpdateTime > MAX_TIME_OUT) {
            if (!listAPI.isEmpty()) {
                for(ObjSendResultEPMToMBF obj : listAPI){
                    if(obj != null && obj.getUrl() != null && !"".equals(obj.getUrl())) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
                        resttemplateBean.handleHttpRequest(obj.getUrl(), 30000, HttpMethod.POST, headers, obj.getBody(), String.class, obj.getTransId());
                    }
                }
                listAPI.clear();
            }
            lastUpdateTime = System.currentTimeMillis();
        }
    }
}
