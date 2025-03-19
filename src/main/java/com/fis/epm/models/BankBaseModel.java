package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankBaseModel implements Serializable{
	private long id = 0L;
	private String code;
	private String name;
	private String type;
	private String bankGate;
}
