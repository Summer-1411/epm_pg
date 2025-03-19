package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BankModel extends BankBaseModel implements Serializable {
	private String token ;
}
