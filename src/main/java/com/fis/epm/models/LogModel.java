package com.fis.epm.models;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogModel implements Serializable {
	private long id = 0;
	private String posCode;
	private String cusName;
	private Date eventTime;
	private String bankCode;
	private String transactionId;
	private String sourceObject;
	private int promotion = 0;
	private double amount = 0;
	private double discount = 0;
	private int migs = 0;
	private String desObject;
	private String webReturnUrlSucc;
	private String webReturnUrlErr;
	private String status;
	private String message;

	@Override
	public String toString() {
		return id + "," + posCode + "," + cusName + "," + eventTime + "," + bankCode + "," + transactionId + ","
				+ sourceObject + "," + promotion + "," + amount + "," + discount + "," + migs + "," + desObject + ","
				+ webReturnUrlSucc + "," + webReturnUrlErr + "," + status + "," + message;
	}

}
