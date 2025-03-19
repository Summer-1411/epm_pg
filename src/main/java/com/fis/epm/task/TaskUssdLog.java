package com.fis.epm.task;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fis.epm.entity.UssdLog;
import com.fis.epm.repo.UssdLogRepo;
import com.fis.epm.utils.EPMQueueManager;

@Component
public class TaskUssdLog {
	private static final Logger logger = LoggerFactory.getLogger(TaskUssdLog.class);
	private static final int MAX_LOG_SIZE = 100;
	private static final long MAX_TIME_OUT = 5 * 1000; // 5 seconds
	private static final List<UssdLog> LOG_USSD = new ArrayList<>();
	private long lastUpdateTime = System.currentTimeMillis();
	@Autowired
	private UssdLogRepo ussdLogRepo;
	
	@Scheduled(fixedDelay = 100)
	public void process() {
		// log call api
		UssdLog log = EPMQueueManager.QUEUE_LOG_USSD.dequeueWait(20);
		if (log != null) {
			LOG_USSD.add(log);
		}
		
		if (LOG_USSD.size() >= MAX_LOG_SIZE || System.currentTimeMillis() - lastUpdateTime > MAX_TIME_OUT) {
			if (!LOG_USSD.isEmpty()) {
				//logResultService.saveLog(LOG_APIS);
				logger.info("Insert log_ussd total = {}", LOG_USSD.size());
				ussdLogRepo.saveAll(LOG_USSD);
				LOG_USSD.clear();
			}
			lastUpdateTime = System.currentTimeMillis();
		}
	}
}
