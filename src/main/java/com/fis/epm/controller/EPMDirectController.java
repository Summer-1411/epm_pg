package com.fis.epm.controller;

import javax.servlet.http.HttpServletRequest;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fis.epm.business.EPMDirectBussiness;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.utils.Utils;

@RestController
@RequestMapping("service")
public class EPMDirectController extends EPMBasicController {

	@Autowired
	private EPMDirectBussiness directBussiness;

	@PostMapping("/bill-transaction")
	public String billTransaction(@RequestParam(name = "transactionId", required = true) String pay_no,
			@RequestParam(name = "amount", required = true) Long amount,
			@RequestParam(name = "toIsdn", required = true) String PhoneNumber,
			@RequestParam(name = "bankCode", required = true) String bankCode, HttpServletRequest request) {
		String url = "/service/bill-transaction";
		if (PhoneNumber.startsWith("0")) {
			PhoneNumber = PhoneNumber.substring(1, PhoneNumber.length());
		}
		String ipRequest = getIpAddress(request);
		return directBussiness.transaction(pay_no, amount, PhoneNumber, bankCode, url, ipRequest);
	}

	@PostMapping("/bill-transaction-old")
	public String billTransactionOld(@RequestParam(name = "pay_no", required = true) String pay_no,
			@RequestParam(name = "amount", required = true) Long amount,
			@RequestParam(name = "thanh_toan_noi_dia", required = false) String card_type,
			@RequestParam(name = "PhoneNumber", required = true) String PhoneNumber,
			@RequestParam(name = "code_bank", required = true) String bankCode,
			@RequestParam(name = "merchant_code", required = false) String merchant_code,
			@RequestParam(name = "pay_type", required = false) String pay_type,
			@RequestParam(name = "language", required = false) String language,
			@RequestParam(name = "object_type", required = false) String object_type,
			@RequestParam(name = "SOURCE_CODE", required = false) String SOURCE_CODE,
			@RequestParam(name = "type_login", required = false) String type_login,
			@RequestParam(name = "type_pay", required = false) String type_pay,
			@RequestParam(name = "token_create", required = false) String token_create,
			@RequestParam(name = "token_id", required = false) String token_id,
			@RequestParam(name = "from_msisdn", required = false) String from_msisdn, HttpServletRequest request) {
		String url = "/service/bill-transaction-old";
		if (PhoneNumber.startsWith("0")) {
			PhoneNumber = PhoneNumber.substring(1, PhoneNumber.length());
		}
		String ipRequest = getIpAddress(request);
		return directBussiness.transaction(pay_no, amount, PhoneNumber, bankCode, url, ipRequest);
	}

	@SneakyThrows
	@GetMapping("/test")
	public Object test(HttpServletRequest request) {
		return directBussiness.testApi();
	}

}
