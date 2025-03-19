package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IsdnInfoApi implements Serializable{
	private String info;
	private String debit;
}
