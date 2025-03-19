package com.fis.epm.napas.models;

import java.io.Serializable;
import java.util.Date;

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
public class OrderModel implements Serializable {

	@JsonProperty("id")
	private String id;

	@JsonProperty("amount")
	private Long amount;

	@JsonProperty("currency")
	private String currency;

	@JsonProperty("creationTime")
	private Date creationTime;

	@JsonProperty("reference")
	private String reference;

	@JsonProperty("totalAuthorizedAmount")
	private Double totalAuthorizedAmount;

	@JsonProperty("totalCapturedAmount")
	private Double totalCapturedAmount;

	@JsonProperty("totalRefundedAmount")
	private Double totalRefundedAmount;
	
	@JsonProperty("authenticationStatus")
	private String authenticationStatus;
	
	@JsonProperty("merchantCategoryCode")
	private String merchantCategoryCode;
	
	@JsonProperty("lastUpdatedTime")
	private Date lastUpdatedTime;
	
	@JsonProperty("merchantAmount")
	private Long merchantAmount;
	
	@JsonProperty("merchantCurrency")
	private String merchantCurrency;
	
	@JsonProperty("chargeback")
	private ChargeBackModel chargeback;
}
