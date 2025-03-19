package com.fis.epm.request.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionRequestModel implements Serializable{
	private String bankCode;
	private String isdn;
}
