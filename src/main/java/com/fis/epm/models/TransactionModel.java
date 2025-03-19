package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionModel implements Serializable{
	
	//private String transactionOrderId;
	private String gateOrderId;
	
	
	//private String toIsdn;
	private String toIsdn;
	
	private double amount = 0;
	
	//private int internationalCard = 0;
	private String cardType;
	
	//private ...
	private String staDate;
	
	
	private String bankCode;
	
	//private String merchantCode;
	private String partnerCode;
	
	
	private String language;
	
	//private String discount
	private double discountAmount = 0;
	
	private String merchanCode;
	
	private String promotionAmount;
	
	private String payStatus;
	 

}