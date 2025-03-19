package com.fis.epm.napas.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NapasCardExpiryModel implements Serializable{
	private int month = 0;
	private int year = 0;
}	
