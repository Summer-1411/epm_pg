package com.fis.epm.napas.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SecureModel implements Serializable{
	private String enrollmentStatus;
	private String xid;
}
