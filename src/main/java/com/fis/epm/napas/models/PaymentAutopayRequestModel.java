package com.fis.epm.napas.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentAutopayRequestModel extends PaymentBaseModel implements Serializable {
	@JsonProperty("sourceOfFunds")
	private SourceOfFundsModel sourceOfFunds;
	
	@JsonProperty("channel")
	private String channel;
	
	@JsonProperty("serviceCode")
	private String serviceCode;
	
}
