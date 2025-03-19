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
public class PaymentResult implements Serializable{
	
	@JsonProperty("apiOperation")
	private String apiOperation;
	
	@JsonProperty("merchantId")
	private String merchantId;
	
	@JsonProperty("order")
	private OrderModel order;
	
	@JsonProperty("response")
	private TokenResultResponse response;
	
	@JsonProperty("result")
	private String result;
	
	@JsonProperty("sourceOfFunds")
	private SourceOfFundsModel_2 sourceOfFunds;
	
	@JsonProperty("transaction")
	private NapasTransactionModel transaction;

	@JsonProperty("orderId")
	private String orderId;

}
