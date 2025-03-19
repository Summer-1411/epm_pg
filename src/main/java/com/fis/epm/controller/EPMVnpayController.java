package com.fis.epm.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fis.epm.business.EPMVnpayCommunication;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.web.html.EpmErrorHtml;

import lombok.SneakyThrows;

@RestController
@RequestMapping(EPMApiConstant.EPM_COMM_ROOT_API_MAPPING)
public class EPMVnpayController extends EPMBasicController {
	private static final Logger log = LoggerFactory.getLogger(EPMVnpayController.class);

	@Autowired
	EPMVnpayCommunication epmVnpayCommunicationService;

	@Autowired
	private EpmErrorHtml epmErrorHtml = null;

	@Value(EPMApplicationProp.NAPAS_URL_ERROR_PROP)
	private String napasUrlError = "";
	
	@Value(EPMApplicationProp.VNPAY_DELAY_CALLBACK)
	private long vnpayDelayCallBack = 0;

	@SneakyThrows
	@PostMapping("/vnpay")
	public void vnpay(HttpServletRequest request, HttpServletResponse response) {
		response.sendRedirect(epmVnpayCommunicationService.payment(request));
	}

	@GetMapping("/vnpay-callback")
	public void vnpayCallback(@RequestParam(name = "vnp_TmnCode") String vnp_TmnCode,
			@RequestParam(name = "vnp_TxnRef") String vnp_TxnRef, @RequestParam(name = "vnp_Amount") String vnp_Amount,
			@RequestParam(name = "vnp_OrderInfo") String vnp_OrderInfo,
			@RequestParam(name = "vnp_ResponseCode") String vnp_ResponseCode,
			@RequestParam(name = "vnp_BankCode") String vnp_BankCode,
			@RequestParam(name = "vnp_BankTranNo", required = false) String vnp_BankTranNo,
			@RequestParam(name = "vnp_CardType", required = false) String vnp_CardType,
			@RequestParam(name = "vnp_PayDate") String vnp_PayDate,
			@RequestParam(name = "vnp_TransactionNo") String vnp_TransactionNo,
			@RequestParam(name = "vnp_TransactionStatus") String vnp_TransactionStatus,
			@RequestParam(name = "vnp_SecureHash") String vnp_SecureHash, HttpServletResponse response,
			HttpServletRequest request) throws Exception {
		String ipRequest = getIpAddress(request);
		String urlReturn = (String) epmVnpayCommunicationService.vnpayCallback(vnp_TmnCode, vnp_TxnRef, vnp_Amount,
				vnp_OrderInfo, vnp_ResponseCode, vnp_BankCode, vnp_BankTranNo, vnp_CardType, vnp_PayDate,
				vnp_TransactionNo, vnp_TransactionStatus, vnp_SecureHash, ipRequest);
		Thread.sleep(vnpayDelayCallBack);
		response.sendRedirect(urlReturn);
	}

	@GetMapping("/vnpay-ipn")
	public Map vnpayIPN(@RequestParam(name = "vnp_TmnCode") String vnp_TmnCode,
			@RequestParam(name = "vnp_TxnRef") String vnp_TxnRef, @RequestParam(name = "vnp_Amount") String vnp_Amount,
			@RequestParam(name = "vnp_OrderInfo") String vnp_OrderInfo,
			@RequestParam(name = "vnp_ResponseCode") String vnp_ResponseCode,
			@RequestParam(name = "vnp_BankCode") String vnp_BankCode,
			@RequestParam(name = "vnp_BankTranNo", required = false) String vnp_BankTranNo,
			@RequestParam(name = "vnp_CardType", required = false) String vnp_CardType,
			@RequestParam(name = "vnp_PayDate") String vnp_PayDate,
			@RequestParam(name = "vnp_TransactionNo") String vnp_TransactionNo,
			@RequestParam(name = "vnp_TransactionStatus") String vnp_TransactionStatus,
			@RequestParam(name = "vnp_SecureHash") String vnp_SecureHash, HttpServletResponse response,
			HttpServletRequest request) throws Exception {
		String ipRequest = getIpAddress(request);
		return (Map) epmVnpayCommunicationService.acceptVnpay(vnp_TmnCode, vnp_TxnRef, vnp_Amount, vnp_OrderInfo,
				vnp_ResponseCode, vnp_BankCode, vnp_BankTranNo, vnp_CardType, vnp_PayDate, vnp_TransactionNo,
				vnp_TransactionStatus, vnp_SecureHash, ipRequest);
	}

	@SneakyThrows
	@GetMapping("/error")
	public String vnpayError(HttpServletRequest request, HttpServletResponse response) {
		return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công",
				"Giao dịch không thành công");
	}
}
