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
public class TokenResult implements Serializable{
	
	@JsonProperty("result")
	private String result;
	
	@JsonProperty("response")
	private TokenResultResponse response;
	
	@JsonProperty("token")
	private String token;
	
	@JsonProperty("card")
	private TokenResultCard card;
	
	@JsonProperty("deviceId")
	private String deviceId;
	
	@JsonProperty("orderId")
	private String orderId;

}
