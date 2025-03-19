package com.fis.epm.napas.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestModel extends PaymentBaseModel implements Serializable {
	private InputParametersModel inputParameters;
}
