package com.fis.epm.utils;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fis.epm.business.EPMBusiness;
import com.fis.epm.entity.EpmTransaction;
import com.fis.epm.models.CallBackUrlModel;
//import com.fis.epm.mq.services.MQPushDataEnqueueService;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMMessageCode;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.DirectModelCallBack;
import com.fis.pg.epm.models.DirectModelCallBackModel;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.epm.models.ObjSendResultEPMToMBF;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DirectUtils {
	
	@Autowired
	private EPMBusiness epmBusiness = null;
	
	@Autowired
	private ResttemplateBean resttemplateBean = null;
	private ObjectMapper mapper;
	
//	@Autowired
//	private MQPushDataEnqueueService mqpushDataEnqueueService = null;
	
	@Autowired
	@Qualifier(EPMApiConstant.APP_MESSAGE_DICTIONARY_BEAN)
	private ConcurrentHashMap<String, String> messageDictionary = null;
    
    private String loadMessage(String status) {
		if (this.messageDictionary == null)
			return "";
		if (this.messageDictionary.containsKey(status))
			return this.messageDictionary.get(status);
		return "";
	}
	
	public DirectUtils() {
		mapper = new ObjectMapper();
	}
	
	public void sendCallBackUrlSuccess(EpmTransaction transaction) {
		try {
		CallBackUrlModel backUrlModel = CallBackUrlModel.builder()
				.tranid(transaction.getTransactionId())
				.status(EPMMessageCode.API_SUCCESSED_CODE)
				.message(loadMessage(EPMMessageCode.API_SUCCESSED_CODE))
				.build();
		String userName = transaction.getUserName();
		String key = epmBusiness.getKeyCheckSum(userName);
		String data = Tools.convertDataToBase64(backUrlModel);
		String checkSum = Tools.getCheckSum(data, key);
		DirectModelCallBackModel model = DirectModelCallBackModel.builder()
				.data(data)
				.checksum(checkSum)
				.build();
		ObjSendResultEPMToMBF obj = new ObjSendResultEPMToMBF();
		obj.setUrl(transaction.getCallBackUrl());
		obj.setBody(model);
		obj.setTransId(transaction.getTransactionId());
//		this.mqpushDataEnqueueService.sendRequestDirect(obj);
		EPMQueueManager.QUEUE_SEC_RESULT_EPM_TO_MBF.enqueueNotify(obj);
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		log.info("---> Trả kết quả thanh toán cho mobifone" + mapper.writeValueAsString(model));
//		Long startTime = System.currentTimeMillis();
//		log.info("start: "+startTime);
//		Object object = (Object) this.resttemplateBean.handleHttpRequestNoReadTimeOut(transaction.getCallBackUrl(),
//				40, HttpMethod.POST, headers, model, Object.class, transaction.getTransactionId());
//		Long endTime = System.currentTimeMillis();
//		Long processTime = endTime - startTime;
//		log.info("---> end" +endTime +"processTime: "+ processTime);
		} catch (Exception e) {
			log.error("Không gửi được kết quả sang mobifone, lỗi "+ e.getMessage(), e);
		}
	}
	
	public void sendCallBackUrlError(EpmTransaction transaction, String partner, String code, String message) {
		try {
			CallBackUrlModel backUrlModel = new CallBackUrlModel();
			if(partner.equalsIgnoreCase(EmpStatusConstain.PARTNER_CODE_VNPAY)) {
				backUrlModel = CallBackUrlModel.builder()
						.tranid(transaction.getTransactionId())
						.status(partner+"-"+code)
						.message(Tools.stringNvl(loadMessage(partner+"-"+code), partner+"-"+code))
						.build();
			}
			if(partner.equalsIgnoreCase(EmpStatusConstain.PARTNER_CODE_NAPAS)) {
				backUrlModel = CallBackUrlModel.builder()
						.tranid(transaction.getTransactionId())
						.status(partner+"-"+code)
						.message(message)
						.build();
			}
			String userName = transaction.getUserName();
			String key = epmBusiness.getKeyCheckSum(userName);
			String data = Tools.convertDataToBase64(backUrlModel);
			String checkSum = Tools.getCheckSum(data, key);
			DirectModelCallBackModel model = DirectModelCallBackModel.builder()
					.data(data)
					.checksum(checkSum)
					.build();
			ObjSendResultEPMToMBF obj = new ObjSendResultEPMToMBF();
			obj.setUrl(transaction.getCallBackUrl());
			obj.setBody(model);
			obj.setTransId(transaction.getTransactionId());
			EPMQueueManager.QUEUE_SEC_RESULT_EPM_TO_MBF.enqueueNotify(obj);
//			this.mqpushDataEnqueueService.sendRequestDirect(obj);
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//			Long startTime = System.currentTimeMillis();
			log.info("---> Trả kết quả thanh toán cho mobifone" + mapper.writeValueAsString(model));
//			log.info("start: "+startTime);
//			Object object = (Object) this.resttemplateBean.handleHttpRequestNoReadTimeOut(transaction.getCallBackUrl(),
//					40, HttpMethod.POST, headers, model, Object.class, transaction.getTransactionId());
//			Long endTime = System.currentTimeMillis();
//			Long processTime = endTime - startTime;
//			log.info("---> end" +endTime +"processTime: "+ processTime);
		} catch (Exception e) {
			log.error("Không gửi được kết quả sang mobifone, lỗi "+ e.getMessage(), e);
		}
	}
}
