package com.fis.epm.napas.models;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentInterResponseModel extends PaymentBaseModel implements Serializable {
	private String result;
	private String dataKey;
	private String napasKey;
	private String merchantId;
	private AuthorizationResponseModel authorizationResponse;
	private NapasPaymentResponseModel response;
	private Date timeOfRecord;
	private NapasTransactionModel transaction;
	private String channel;
	private String version;
}
