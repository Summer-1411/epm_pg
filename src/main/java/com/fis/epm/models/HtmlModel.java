package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HtmlModel implements Serializable {
	private String apiOperation;
	private String title;
	private String cardScheme;
	private String enable3DSecure;
	private String orderId;
	private String dataKey;
	private String napasKey;
	private long orderAmount = 0;
	private String orderCurrency;
	private String orderReference;
	private String channel;
	private String sourceOfFundsType;
	private String clientIP;
	private String urlReturn;
	private String language;
	private String agreementType;
	private String agreementId;
	private String agreementExpiryDate;
	private String agreementDaysBetweenPayments;
}
