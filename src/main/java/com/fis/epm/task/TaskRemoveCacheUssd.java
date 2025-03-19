package com.fis.epm.task;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fis.epm.entity.UssdMessage;
import com.fis.epm.entity.UssdServiceCode;
import com.fis.epm.entity.UssdSubType;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.pg.common.utils.Tools;

@Component
public class TaskRemoveCacheUssd {
	private static final Logger logger = LoggerFactory.getLogger(TaskRemoveCacheUssd.class);
	
	@Value(EPMApplicationProp.USSD_TIME_EXPIRE_MAP_USSD_MENU)
	private long timeExpireMapUssdMenu;
	
	@Value(EPMApplicationProp.USSD_TIME_EXPIRE_MAP_USSD_MESSAGE)
	private long timeExpireMapUssdMessage;
	
	@Scheduled(fixedDelay = 10000)
	public void process() {
		long currentTime = System.currentTimeMillis();
		List<String> allKeyMenu = EPMBaseCommon.mapUssdMenu.keySet().stream().collect(Collectors.toList());
		for(String key : allKeyMenu) {
			UssdServiceCode menu = EPMBaseCommon.mapUssdMenu.get(key);
			long expireTime = menu.getCreateDate() + timeExpireMapUssdMenu;
			if(expireTime < currentTime) {
				EPMBaseCommon.mapUssdMenu.remove(key);
				logger.info("Remove menu : " + Tools.convertModeltoJSON(menu));
			}
		}
		
		List<String> allKeyMessage = EPMBaseCommon.mapUssdMessage.keySet().stream().collect(Collectors.toList());
		for(String key : allKeyMessage) {
			UssdMessage message = EPMBaseCommon.mapUssdMessage.get(key);
			long expireTime = message.getCreateTime() + timeExpireMapUssdMessage;
			if(expireTime < currentTime) {
				EPMBaseCommon.mapUssdMessage.remove(key);
				logger.info("Remove message : " + Tools.convertModeltoJSON(message));
			}
		}
		
		List<String> allKeySubType = EPMBaseCommon.mapUssdSubType.keySet().stream().collect(Collectors.toList());
		for(String key : allKeySubType) {
			UssdSubType mapUssdSubType = EPMBaseCommon.mapUssdSubType.get(key);
			long expireTime = mapUssdSubType.getCreateTime() + timeExpireMapUssdMenu;
			if(expireTime < currentTime) {
				EPMBaseCommon.mapUssdSubType.remove(key);
				EPMBaseCommon.mapLastMessage.remove(key);
				EPMBaseCommon.mapLanguage.remove(key);
				logger.info("Remove subType : " + Tools.convertModeltoJSON(mapUssdSubType));
			}
		}
	}
}
