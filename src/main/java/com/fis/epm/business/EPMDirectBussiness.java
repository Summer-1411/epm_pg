package com.fis.epm.business;

public interface EPMDirectBussiness {

	public String transaction(String pay_no, Long amount, String PhoneNumber, String bankCode, String url,
			String ipRequest);

	public Object testApi() throws Exception;
}
