package com.fis.epm.napas.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentInterResponseModelOTP extends PaymentBaseModel implements Serializable {
	@JsonProperty("merchantId")
	private String merchantId;
	
	@JsonProperty("response")
	private TokenResultResponse response;
	
	@JsonProperty("result")
	private String result;
	
	@JsonProperty("acsMode")
	private String acsMode;
	
	@JsonProperty("3DSecure")
	private Secure3DModelResponse secure3dModel;
	
	@JsonProperty("3DSecureId")
	private String secure3DId;
}
