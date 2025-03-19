package com.fis.epm.napas.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentInterRequestModel extends PaymentBaseModel implements Serializable {
	
	@JsonProperty("channel")
	private String channel;
	
	@JsonProperty("sourceOfFunds")
	private SourceOfFundsModel sourceOfFunds;
	
	@JsonProperty("3DSecure")
	private Secure3DModel secure3dModel;
	
	@JsonProperty("inputParameters")
	private InputParametersModel inputParameters;
}
