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
public class PaymentWithOTPResponseModel extends PaymentBaseModel implements Serializable {

	@JsonProperty("merchantId")
	private String merchantId;

	@JsonProperty("response")
	private NapasPaymentResponseModel response;

	@JsonProperty("result")
	private String result;

	@JsonProperty("sourceOfFunds")
	private SourceOfFundsModel_2 sourceOfFunds;

	@JsonProperty("transaction")
	private NapasTransactionModel transaction;

	@JsonProperty("version")
	private String version;

	@JsonProperty("channel")
	private String channel;

	@JsonProperty("dataKey")
	private String dataKey;

	@JsonProperty("napasKey")
	private String napasKey;

	@JsonProperty("tokenResult")
	private TokenResultModel tokenResult;

	@JsonProperty("paymentResult")
	private TokenResultModel paymentResult;

	@JsonProperty("error")
	private TokenResultErrorModel error;

	@JsonProperty("token")
	private String token;
}
