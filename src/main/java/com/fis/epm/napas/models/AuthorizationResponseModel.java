package com.fis.epm.napas.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthorizationResponseModel implements Serializable {
	@JsonProperty("date")
	private String date;
	
	@JsonProperty("financialNetworkDate")
	private String financialNetworkDate;
	
	@JsonProperty("processingCode")
	private String processingCode;
	
	@JsonProperty("responseCode")
	private String responseCode;
	
	@JsonProperty("returnAci")
	private String returnAci;
	
	@JsonProperty("stan")
	private String stan;
	
	@JsonProperty("time")
	private String time;
	
	@JsonProperty("commercialCard")
	private String commercialCard;
	
	@JsonProperty("commercialCardIndicator")
	private String commercialCardIndicator;
	
	@JsonProperty("posData")
	private String posData;
	
	@JsonProperty("posEntryMode")
	private String posEntryMode;
	
	@JsonProperty("transactionIdentifier")
	private String transactionIdentifier;

}
