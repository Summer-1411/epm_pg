package com.fis.epm.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AutoDebitModel {
	private String msisdn;
	private String subType;
	private String amount; // LONG
	private String thresholdAmount;//LONG
	private String paymentDay;
	private String tokenizer;
	private String timeResgister;
	private String migs;
	private String codeBank;
	private String subId;
	private String fromIsdn;
	private String subIdReg;
	
	private String startDate;
}
