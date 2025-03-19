package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromotionModel implements Serializable{
	private String type;
	private String value ;
	private String method ;
	private String pReturn;
}
