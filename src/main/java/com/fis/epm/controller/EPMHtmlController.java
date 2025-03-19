package com.fis.epm.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fis.epm.business.EpmTransactionBusiness;
import com.fis.epm.napas.models.NapasResult;
import com.fis.epm.napas.models.PaymentResult;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.pg.epm.models.EpmCreateOrderRequestModel;
import com.fis.pg.gw.server.models.ResponseModel;
import com.google.gson.Gson;

@RestController
@RequestMapping("/epm-html")
public class EPMHtmlController extends EPMBasicController{

	@Autowired
	private EpmTransactionBusiness epmTransactionBusiness = null;

	@PostMapping("/create-order")
	public ResponseModel createOrder(@RequestBody EpmCreateOrderRequestModel req) {
		return this.epmTransactionBusiness.createOrder(req);
	}

	/**
	 * 
	 * @param res
	 * @return
	 * 
	 *         API dung cho loại thanh toan voi apiOperation="PAY" va
	 *         apiOperation="PAY_WITH_RETURNED_TOKEN"
	 * 
	 */
	@PostMapping("/napas-html")
	public String loadNapasHtml(HttpServletRequest request, HttpServletResponse res) {
		res.setContentType("text/html; charset=UTF-8");
		return this.epmTransactionBusiness.createTransaction(request);
	}

	/**
	 * 
	 * 
	 * @param napasResult: napas ban du lieu dang form nen dung @RequestParam
	 * @throws IOException
	 * 
	 */
	@PostMapping("/napas-form-callback")
	public void acceptRequest(@RequestParam(name = "napasResult") String napasResult, HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		String ipRequest = EPMBaseCommon.getIpAddress(request);
		String url = "/napas-form-callback";
		Gson gsonNapasResult = new Gson();
		NapasResult napasResultICallback = gsonNapasResult.fromJson(napasResult, NapasResult.class);
		response.sendRedirect(epmTransactionBusiness.acceptRequest(napasResultICallback, ipRequest, url));
	}
	
	@PostMapping("/napas-ipn-callback")
	public ResponseModel acceptRequestIpn(@RequestBody NapasResult napasResult, HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		ResponseModel res = new ResponseModel();
		try {
			String ipRequest = EPMBaseCommon.getIpAddress(request);
			String url = "/napas-ipn-callback";
			epmTransactionBusiness.acceptRequest(napasResult, ipRequest, url);
			res = this.buildSuccessedResponse(null);
		} catch (Exception e) {
			res = this.buildExceptionResponse(e);
		}
		return res;
	}

	@GetMapping("/napas-token-html")
	public String loadNapasHtmlToken(HttpServletRequest request, HttpServletResponse res) {
		res.setContentType("text/html; charset=UTF-8");
		return this.epmTransactionBusiness.createTransactionToken(request);
	}

	@PostMapping("/napas-form-callback-token")
	public void acceptRequestToken(@RequestParam(name = "napasResult") String napasResult, HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		String ipRequest = EPMBaseCommon.getIpAddress(request);
		String url = "/napas-form-callback-token";
		Gson gsonNapasResult = new Gson();
		NapasResult napasResultICallback = gsonNapasResult.fromJson(napasResult, NapasResult.class);
		response.sendRedirect(epmTransactionBusiness.acceptRequestToken(napasResultICallback, ipRequest, url));
	}

	@GetMapping("/error")
	public String error(HttpServletRequest request, HttpServletResponse res) {
		res.setContentType("text/html; charset=UTF-8");
		return this.epmTransactionBusiness.error(request);
	}

	@PostMapping("/accept-request-retryDR")
	public void acceptRequest(@RequestBody PaymentResult napasResult, HttpServletResponse response) throws IOException {
		epmTransactionBusiness.acceptRequestRetryDR(napasResult);
	}
}
