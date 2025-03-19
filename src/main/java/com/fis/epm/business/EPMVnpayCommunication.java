package com.fis.epm.business;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface EPMVnpayCommunication {

	public String payment(HttpServletRequest req);

	public Map acceptVnpay(String vnp_TmnCode, String vnp_TxnRef, String vnp_Amount, String vnp_OrderInfo,
			String vnp_ResponseCode, String vnp_BankCode, String vnp_BankTranNo, String vnp_CardType,
			String vnp_PayDate, String vnp_TransactionNo, String vnp_TransactionStatus, String vnp_SecureHash,
			String ipRequest);

	public String vnpayCallback(String vnp_TmnCode, String vnp_TxnRef, String vnp_Amount, String vnp_OrderInfo,
			String vnp_ResponseCode, String vnp_BankCode, String vnp_BankTranNo, String vnp_CardType,
			String vnp_PayDate, String vnp_TransactionNo, String vnp_TransactionStatus, String vnp_SecureHash,
			String ipRequest);
}
