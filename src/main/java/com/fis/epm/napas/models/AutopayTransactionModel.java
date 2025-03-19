package com.fis.epm.napas.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutopayTransactionModel implements Serializable{
	
	@JsonProperty("acquirer")
	private AutopayTransactionAcquirerModel acquirer;
	
	@JsonProperty("amount")
	private String amount;
	
	@JsonProperty("authorizationCode")
	private String authorizationCode;
	
	@JsonProperty("currency")
	private String currency;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("receipt")
	private String receipt;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("authenticationStatus")
	private String authenticationStatus;
	
	@JsonProperty("stan")
	private String stan;

}
