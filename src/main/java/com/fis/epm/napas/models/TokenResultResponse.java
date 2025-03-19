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
public class TokenResultResponse implements Serializable{
	
	@JsonProperty("gatewayCode")
	private String gatewayCode;
	
	@JsonProperty("3DSecure")
	private Secure3DgatewayCode secure3DgatewayCode;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("acquirerCode")
	private String acquirerCode;
	
	@JsonProperty("acquirerMessage")
	private String acquirerMessage;

}
