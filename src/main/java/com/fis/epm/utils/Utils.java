package com.fis.epm.utils;

import java.util.Date;

import com.fis.epm.entity.LogApiResult;
import com.fis.fw.common.utils.StringUtil;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.gw.server.models.EpmMerchantLogModel;

public class Utils {
	public static LogApiResult initLog(String method, String uri, String requestHeader, String requestBody, Integer userId) {
		LogApiResult log = new LogApiResult();
		log.setMethod(method);
		log.setUri(uri);
		log.setRequestHeader(requestHeader);
		log.setRequestBody(requestBody);
		log.setUserId(userId);
		log.setCreateTime(new Date());
		return log;
	}

	public static EpmMerchantLogModel genMerchantLog(EpmTransactionModel data){
		EpmMerchantLogModel merchantLog = new EpmMerchantLogModel();
		merchantLog.setTransactionId(data.getTransactionId());
		merchantLog.setUserName(data.getUserName());
		merchantLog.setBankCode(data.getBankCode());
		merchantLog.setPayStatus(data.getPayStatus());
		merchantLog.setRequestDate(data.getStaDateTime());
		merchantLog.setResponseDate(new Date());
		merchantLog.setCardType(data.getCardType());
		merchantLog.setDescription(data.getDescription());
		merchantLog.setAmount(data.getPayAmount());
		merchantLog.setPartnerCode(data.getPartnerCode());
		merchantLog.setResponseCode(data.getResponseCode());
		if (EmpStatusConstain.SUCC.equals(data.getPayStatus())) {
			merchantLog.setCardNumber(data.getCardNumber());
			merchantLog.setCardHolder(data.getCardHolder());
			merchantLog.setTokenId(StringUtil.nvl(data.getTokenId(),""));
		}
		return merchantLog;
	}
}
