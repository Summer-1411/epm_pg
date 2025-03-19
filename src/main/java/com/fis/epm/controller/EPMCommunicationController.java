package com.fis.epm.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fis.epm.service.PGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fis.epm.business.impl.EPMNapasCommunicationService;
import com.fis.epm.napas.models.NapasRefundDomesticModel;
import com.fis.epm.napas.models.NapasResult;
import com.fis.epm.napas.models.PaymentAutopayRequestModel;
import com.fis.epm.napas.models.PaymentInterRequestModel;
import com.fis.epm.napas.models.PaymentRequestModel;
import com.fis.epm.napas.models.PaymentWithOTPRequestModel;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.pg.gw.server.models.ResponseModel;

@RestController
@RequestMapping(EPMApiConstant.EPM_COMM_ROOT_API_MAPPING)
@CrossOrigin
public class EPMCommunicationController extends EPMBasicController {

	private static final Logger log = LoggerFactory.getLogger(EPMCommunicationController.class);

	@Autowired
	private EPMNapasCommunicationService napasComm = null;

	@Autowired
	private PGService pgService;

	/**
	 * test login, when deploy have to remove
	 */
	@GetMapping("/login-napas")
	public ResponseModel loginNapas() {
		ResponseModel res = new ResponseModel();

		try {
			res = this.buildSuccessedResponse(this.napasComm.login());
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}

	@GetMapping("/payment-napas")
	public ResponseModel payment(@RequestBody PaymentRequestModel paymentRequestModel) {
		//		request nhap nhu sau
		//		"order": {
		//        "id": "ORD_10001",
		//        "amount": 310000.0,
		//        "currency": "VND"
		//    	}
		ResponseModel res = new ResponseModel();

		try {
			res = this.buildSuccessedResponse(this.napasComm.paymentAtmLocal(paymentRequestModel));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}

	@GetMapping("/payment-napas-inernational")
	public ResponseModel paymentAtmInernational() {
		ResponseModel res = new ResponseModel();

		try {
			res = this.buildSuccessedResponse(this.napasComm.paymentAtmInernational());
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}



	@GetMapping("/payment-napas-inernational-3dSercuse")
	public ResponseModel paymentAtmInernational3dSercuse(@RequestBody PaymentInterRequestModel payment, 
														@RequestParam(name="orderId") String orderId, 
														@RequestParam(name="transactionId") String transactionId,
														@RequestParam(name="secureId") String secureId) {
		ResponseModel res = new ResponseModel();

		try {
			res = this.buildSuccessedResponse(this.napasComm.paymentAtmInernationalWith3dSercuse(payment, orderId, transactionId, secureId));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}

	@GetMapping("/payment-napas-token-atm-local-otp")
	public ResponseModel paymentAtmLocalWithOtp(@RequestBody PaymentWithOTPRequestModel paymentWithOTPRequestModel,
												@RequestParam(name = "orderId") String orderId,
												@RequestParam(name = "transactionId") String transactionId) {
		ResponseModel res = new ResponseModel();
		try {
			res = this.buildSuccessedResponse(this.napasComm.paymentAtmLocalWithOtp(paymentWithOTPRequestModel, orderId, transactionId));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}

	@GetMapping("/refund-domestic")
	public ResponseModel refundDomestic(@RequestBody NapasRefundDomesticModel napasRefundDomesticModel,
										@RequestParam(name = "orderId") String orderId,
										@RequestParam(name = "transactionId") String transactionId){
		ResponseModel res = new ResponseModel();
		try {
			res = this.buildSuccessedResponse(this.napasComm.refundDomestic(napasRefundDomesticModel, orderId, transactionId));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}
	@GetMapping("/retrieve-domestic")
	public ResponseModel retrieve(@RequestParam(name = "orderId") String orderId){
		ResponseModel res = new ResponseModel();
		try {
			res = this.buildSuccessedResponse(this.napasComm.retrieveDomestic(orderId));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}
	@GetMapping("/delete-token")
	public ResponseModel deleteToken(@RequestParam(name = "token") String token ){
		ResponseModel res = new ResponseModel();
		try {
			res = this.buildSuccessedResponse(this.napasComm.deleteToken(token));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}
	@GetMapping("/retrieve-token")
	public ResponseModel retrieveToken(@RequestParam(name = "orderId") String orderId){
		ResponseModel res = new ResponseModel();
		try {
			res = this.buildSuccessedResponse(this.napasComm.retrieveToken(orderId));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}
	@GetMapping("/autopay")
	public ResponseModel autopay(@RequestBody PaymentAutopayRequestModel autopayRequestModel, 
								 @RequestParam(name = "orderId") String orderId,
								 @RequestParam(name = "transactionId") String transactionId){
		ResponseModel res = new ResponseModel();
		try {
			res = this.buildSuccessedResponse(this.napasComm.autopay(autopayRequestModel, orderId, transactionId));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}
	
	/**
	 * API accept request from Napas and the other partner
	 */

	@PostMapping("/accept-ipn")
	public ResponseModel acceptPaymentRequestFromPartner(@RequestBody Map req) {
		ResponseModel res = new ResponseModel();

		try {
			res = this.buildSuccessedResponse();
			pushInQueue(req);
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}

	@GetMapping("/test-async")
	@Async(EPMApiConstant.ASYN_EXECUTOR_BEAN)
	public ResponseModel testAsync() {
		// TODO Auto-generated method stub
		ResponseModel res = new ResponseModel();

		try {
			log.info("hello message");
			Thread.sleep(15000);
		} catch (Exception exp) {
			res = buildExceptionResponse(exp);
		}
		return res;
	}
	
	@PostMapping("/napasResult")
	public ResponseModel napasResult(@RequestParam(name = "napasResult") String napasResult) {
		ResponseModel res = new ResponseModel();
		try {
			res = this.buildSuccessedResponse(this.napasComm.napasResult(napasResult));
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}

	@PostMapping("/manualPGExecute")
	public ResponseModel manualPGExecute(@RequestBody List<String> tranID) {
		ResponseModel res = new ResponseModel();
		try {
			pgService.manualExecutePG(tranID);
			res = this.buildSuccessedResponse();
		} catch (Exception exp) {
			res = this.buildExceptionResponse(exp);
		}
		return res;
	}
}
