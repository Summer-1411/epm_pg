package com.fis.epm.napas.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NapasCardModel implements Serializable{
	private String brand;
	private String issuer;
	private String number;
	private String scheme;
	private NapasCardExpiryModel expiry;
}
