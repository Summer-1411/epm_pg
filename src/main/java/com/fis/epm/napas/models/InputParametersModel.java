package com.fis.epm.napas.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputParametersModel implements Serializable{
	private String clientIP;
	private String deviceId;
	private String environment;
	private String cardScheme;
	private Boolean enable3DSecure;
}
