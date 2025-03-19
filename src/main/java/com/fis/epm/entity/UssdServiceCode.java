package com.fis.epm.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UssdServiceCode implements Serializable{
	private String serviceCode;
	private long createDate;
}
