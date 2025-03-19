package com.fis.epm.request.models;

import java.io.Serializable;

import com.fis.pg.anotations.ValidateString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrepaidRequestModel implements Serializable {
	@ValidateString(length = 0, isValidateEmpty = true)
	private String isdn;
	@ValidateString(length = 0, isValidateEmpty = true)
	private String fromIsdn;
	private double amount = 0;
	private double payAmount = 0;
	@ValidateString(length = 0, isValidateEmpty = true)
	private String detail;
//	@ValidateString(length = 0, isValidateEmpty = true)
//	private String cardType;
	private double promotion = 0;
	private double discount = 0;
//	@ValidateString(length = 0, isValidateEmpty = true)
//	private String paymentType;
	@ValidateString(length = 0, isValidateEmpty = true)
	private String bankCode;
//	@ValidateString(length = 0, isValidateEmpty = true)
//	private String merchantCode;
	@ValidateString(length = 0, isValidateEmpty = true)
	private String language;
	//@ValidateString(length = 0, isValidateEmpty = true)
//	private String webReturnUrlSucc;
	//@ValidateString(length = 0, isValidateEmpty = true)
//	private String webReturnUrlErr;
//	@ValidateString(length = 0, isValidateEmpty = true)
//	private String sourceCode;
//	@ValidateString(length = 0, isValidateEmpty = true)
//	private String objectType;
//	@ValidateString(length = 0, isValidateEmpty = true)
//	private String userName;
//	@ValidateString(length =0,isValidateEmpty = true)
	private String callBackUrl ;
//	@ValidateString(length = 0, isValidateEmpty = true)
//	private String tokenPay;
	private String redirectUrl;
	
	@ValidateString(length = 0, isValidateEmpty = true)
	private String tokenCreated;
	private String tokenId;
	private String typeProduct;

	//1: luồng cũ đi qua redis
	//2: luồng mới không qua redis (Đăng ký)
	//COMMENT CODE
	private String typeCreateTransaction;
	private String packageCode;

//	private String partnerCode;
//	private String merchantCode;
//	private String cenCode;
//	private String typeReference;
//	private String objectType;
//	private String paymentType;
}
