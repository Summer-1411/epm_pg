package com.fis.epm.business;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;

import com.fis.epm.napas.models.NapasResult;
import com.fis.epm.napas.models.PaymentResult;
import com.fis.pg.epm.models.EpmCreateOrderRequestModel;
import com.fis.pg.gw.server.models.ResponseModel;

public interface EpmTransactionBusiness {
	public String createTransaction(HttpServletRequest request);

	public ResponseModel createOrder(EpmCreateOrderRequestModel req);

	public String acceptRequest(NapasResult napasResult, String ipRequest, String url);

	public String createTransactionToken(HttpServletRequest request);

	public String acceptRequestToken(NapasResult napasResult, String ipRequest, String url);

	public String error(HttpServletRequest request);

	public String acceptRequestRetryDR(PaymentResult napasResult);

	public ResponseEntity<String> sendUserKyc(Object userKYCDetail);
	
	public Double checkSubtype(String p_strsdt, String transactionId);

}
