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
public class NapasTransactionModel implements Serializable {

	@JsonProperty("acquirer")
	private NapasAcquirerModel acquirer;

	@JsonProperty("amount")
	private double amount = 0;

	@JsonProperty("currency")
	private String currency;

	@JsonProperty("id")
	private String id;

	@JsonProperty("type")
	private String type;

	@JsonProperty("authorizationCode")
	private String authorizationCode;

	@JsonProperty("receipt")
	private String receipt;
}
