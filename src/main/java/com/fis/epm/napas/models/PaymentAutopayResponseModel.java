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
public class PaymentAutopayResponseModel extends PaymentBaseModel implements Serializable{
	@JsonProperty("authorizationResponse")
	private AuthorizationResponseModel authorizationResponse;
	
	@JsonProperty("merchantId")
	private String merchantId;
	
	@JsonProperty("response")
	private ResponseAutopayModel response;
	
	@JsonProperty("result")
	private String result;
	
	@JsonProperty("risk")
	private AutopayRiskModel risk;
	
	@JsonProperty("sourceOfFunds")
	private SourceOfFundsModel sourceOfFunds;
	
	@JsonProperty("timeOfRecord")
	private String timeOfRecord;
	
	@JsonProperty("transaction")
	private AutopayTransactionModel transaction;
	
	@JsonProperty("version")
	private String version;
	
	@JsonProperty("agreement")
	private AutopayAgreementModel agreement;
	
	@JsonProperty("timeOfLastUpdate")
	private String timeOfLastUpdate;
	
	@JsonProperty("error")
	private Object error;
	
	@JsonProperty("error_description")
	private String error_description;
}
