package com.fis.epm.controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMMessageCode;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.LinkQueue;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.gw.server.models.ResponseModel;

import lombok.extern.java.Log;

@CrossOrigin(EPMApiConstant.CROSS_ORIGIN_PROP)
public class EPMBasicController {
	@Autowired
	@Qualifier(EPMApiConstant.APP_MESSAGE_DICTIONARY_BEAN)
	private ConcurrentHashMap<String, String> messageDictionary = null;
	@Autowired
	@Qualifier(EPMApiConstant.ACCEPT_PAYMENT_REQUEST_BEAN)
	private LinkQueue<Map> acceptPaymentRequestQueue = null;
	@Autowired
	private HttpServletRequest request = null;

	private String loadMessage(String status) {
		if (this.messageDictionary == null)
			return "";
		if (this.messageDictionary.containsKey(status))
			return this.messageDictionary.get(status);
		return "";
	}

	protected ResponseModel buildSuccessedResponse(Object payload) {
		ResponseModel res = new ResponseModel();
		res.setPayload(payload);
		res.setStatus(EPMMessageCode.API_SUCCESSED_CODE);
		res.setMessage(loadMessage(EPMMessageCode.API_SUCCESSED_CODE));
		return res;
	}
	
	protected ResponseModel buildResValid(String mess,String code){
		ResponseModel res = new ResponseModel();
		if(mess.equals(""))
			res.setMessage(loadMessage(code));
		else
			res.setMessage(mess);
		res.setStatus(code);
		return res;
	}

	protected ResponseModel buildSuccessedResponse() {
		ResponseModel res = new ResponseModel();
		res.setStatus(EPMMessageCode.API_SUCCESSED_CODE);
		res.setMessage(loadMessage(EPMMessageCode.API_SUCCESSED_CODE));
		return res;
	}

	protected ResponseModel buildExceptionResponse(Exception exp) {
		ResponseModel res = Tools.buildResponseModel(exp);
		//res.setMessage(String.format(loadMessage(EPMMessageCode.API_EXCEPTION_CODE), exp.getMessage()));
		return res;
	}

	protected ResponseModel buildResponse(String status, Object... params) {
		ResponseModel res = new ResponseModel();
		res.setStatus(status);
		res.setMessage(String.format(loadMessage(status), params));
		return res;
	}

	protected ResponseModel buildResponse(String status) {
		ResponseModel res = new ResponseModel();
		res.setStatus(status);
		res.setMessage(loadMessage(status));
		return res;
	}

	protected void pushInQueue(Map req) throws AppException {
		if (this.acceptPaymentRequestQueue == null)
			throw new AppException(EPMMessageCode.QUEUE_ACCEPT_IS_NULL_OR_FULL_CODE);
		if (this.acceptPaymentRequestQueue.getSize() > 480000)
			throw new AppException(EPMMessageCode.QUEUE_ACCEPT_IS_NULL_OR_FULL_CODE);
		this.acceptPaymentRequestQueue.enqueueNotify(req);
	}

	public String getIpAddress(HttpServletRequest requestIp) {
		String remoteAddr = "";
		if (requestIp != null) {
			remoteAddr = requestIp.getHeader("X-FORWARDED-FOR");
			System.out.println("X-FORWARDED-FOR: " + remoteAddr);
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = requestIp.getRemoteAddr();
				System.out.println("getRemoteAddr: " + remoteAddr);
				if (remoteAddr.contains("0:0:0:0:0:")) {
					remoteAddr = "127.0.0.1";
				}
			}
			if (remoteAddr != null && !"".equals(remoteAddr)) {
				String[] ipAddress = remoteAddr.split(",");
				if (ipAddress != null && ipAddress.length > 0) {
					remoteAddr = ipAddress[0];
				}
			}
		}
		return remoteAddr;
	}
}
