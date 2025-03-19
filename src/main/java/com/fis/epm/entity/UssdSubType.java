package com.fis.epm.entity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UssdSubType implements Serializable{
	private String subType;
	private Long createTime;
}
