package com.fis.epm.models;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PGExecuteBodyModel implements Serializable{
	private String fieldId;
	private String value;

	public PGExecuteBodyModel(String fieldId, String value) {
		this.fieldId = fieldId;
		this.value = value;
	}
}
