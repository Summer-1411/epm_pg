package com.fis.epm.task;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.service.LogApiResultService;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.pg.common.utils.ATMUtil;
import com.fis.pg.common.utils.AppConfigurationProp;

@Component
public class TaskLogApiResult {
	private static final Logger logger = LoggerFactory.getLogger(TaskLogApiResult.class);

	private static final int MAX_LOG_SIZE = 100;
	private static final long MAX_TIME_OUT = 5 * 1000; // 5 seconds
	private static final List<LogApiResult> LOG_APIS = new ArrayList<>();
	private long lastUpdateTime = System.currentTimeMillis();

	@Autowired
	private LogApiResultService logResultService;
	
	@Value(AppConfigurationProp.IP_ADDRESS)
	private String ip = "";

	@Scheduled(fixedDelay = 100)
	public void process() {
		// log call api
		int size = EPMQueueManager.QUEUE_LOG_API_RESULT.getSize();
		if (size < 100) {
			for (int i = 0; i < size; i++) {
				com.fis.epm.entity.LogApiResult log = EPMQueueManager.QUEUE_LOG_API_RESULT.dequeueWait(1);
				if (log != null) {
					log.setType(ATMUtil.TYPE_CALL_API_RECEIVE);
					log.setIpServer(ip);
					LOG_APIS.add(log);
				}
			}
		} else {
			for (int i = 0; i < 100; i++) {
				com.fis.epm.entity.LogApiResult log = EPMQueueManager.QUEUE_LOG_API_RESULT.dequeueWait(1);
				if (log != null) {
					log.setType(ATMUtil.TYPE_CALL_API_RECEIVE);
					log.setIpServer(ip);
					LOG_APIS.add(log);
				}
			}
		}
//		com.fis.epm.entity.LogApiResult log = EPMQueueManager.QUEUE_LOG_API_RESULT.dequeueWait(1);
//		if (log != null) {
//			log.setType(ATMUtil.TYPE_CALL_API_RECEIVE);
//			log.setIpServer(ip);
//			LOG_APIS.add(log);
//		}
		int sizeA = EPMQueueManager.QUEUE_LOG_API_SEND.getSize();
		if (sizeA < 100) {
			for (int i = 0; i < sizeA; i++) {
				com.fis.epm.entity.LogApiResult log = EPMQueueManager.QUEUE_LOG_API_SEND.dequeueWait(1);
				if (log != null) {
					if(log.getType() == null) {
						log.setType(ATMUtil.TYPE_CALL_API_SEND);
					}
					log.setIpServer(ip);
					LOG_APIS.add(log);
				}
			}
		} else {
			for (int i = 0; i < sizeA; i++) {
				com.fis.epm.entity.LogApiResult log = EPMQueueManager.QUEUE_LOG_API_SEND.dequeueWait(1);
				if (log != null) {
					if(log.getType() == null) {
						log.setType(ATMUtil.TYPE_CALL_API_SEND);
					}
					log.setIpServer(ip);
					LOG_APIS.add(log);
				}
			}
		}
//		log = EPMQueueManager.QUEUE_LOG_API_SEND.dequeueWait(1);
//		if (log != null) {
//			if(log.getType() == null) {
//				log.setType(ATMUtil.TYPE_CALL_API_SEND);
//			}
//			log.setIpServer(ip);
//			LOG_APIS.add(log);
//		}

		if (LOG_APIS.size() >= MAX_LOG_SIZE || System.currentTimeMillis() - lastUpdateTime > MAX_TIME_OUT) {
			if (!LOG_APIS.isEmpty()) {
				logResultService.saveLog(LOG_APIS);
				logger.info("Insert log_api_result total = {}", LOG_APIS.size());
				LOG_APIS.clear();
			}
			lastUpdateTime = System.currentTimeMillis();
		}
	}
}
