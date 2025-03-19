package com.fis.epm.napas.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NapasLoginResponseModel implements Serializable {
	private String access_token;
	private String token_type;
	private String refresh_token;
	private String expires_in;
	private String scope;
}
