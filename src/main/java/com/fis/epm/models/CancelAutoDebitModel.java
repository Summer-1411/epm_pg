package com.fis.epm.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelAutoDebitModel {
	private String isdn;
	private String message;
	private String code;
}
