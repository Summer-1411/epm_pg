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
public class NapasPaymentResponseModel implements Serializable{

	@JsonProperty("acquirerCode")
	private String acquirerCode;

	@JsonProperty("gatewayCode")
	private String gatewayCode;

	@JsonProperty("acquirerMessage")
	private String acquirerMessage;

	@JsonProperty("message")
	private String message;
}
