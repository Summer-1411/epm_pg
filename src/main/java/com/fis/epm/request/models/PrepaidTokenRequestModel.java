package com.fis.epm.request.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrepaidTokenRequestModel extends PrepaidRequestModel implements Serializable {
	private int typeLogin = 0;
	private int typePay = 0;
	private int tokenCreate = 0;
	private String tokenId;
	private String logId;

}
