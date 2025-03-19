package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PosModel implements Serializable{
	private long shopId;
	private String shopCode;
	private String shopName;
	private String shopAddress;
}
