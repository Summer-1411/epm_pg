package com.fis.epm.models;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenModel implements Serializable{
	private String tokenId;
	private String tokenName;
	private String cardType;
	private String bankCode;
	private String bankOtp;
	private String fromIsdn ;
	private String createDate;
	private String expiredDate;
	List<AutoDebitModel> autoDebitInfo;
}
