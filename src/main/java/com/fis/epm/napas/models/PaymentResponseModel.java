package com.fis.epm.napas.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentResponseModel extends PaymentBaseModel implements Serializable {
	private String result;
	private String dataKey;
	private String napasKey;
}
