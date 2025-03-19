package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChargeModel implements Serializable{
	private String name;
	private long value = 0L;
	private String status ;
}
