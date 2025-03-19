package com.fis.epm.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UssdMessage implements Serializable{
	private List<String> lstMessage;
	private long createTime;
	private int page;
	public UssdMessage() {
		super();
		this.createTime = System.currentTimeMillis();
	}
}
