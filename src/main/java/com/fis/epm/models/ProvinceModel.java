package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProvinceModel implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long payAreaId;
	private String payAreaCode;
	private String name;
}
