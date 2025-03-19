package com.fis.epm.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fis.epm.napas.models.AutopayRiskModel;
import com.fis.epm.napas.models.AutopayRiskResponseModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubtypeResponseModel implements Serializable {
	
	@JsonProperty("code")
	private String code;
	
	@JsonProperty("paramCode")
	private String paramCode;
	
	@JsonProperty("message")
	private String message;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("data")
	private SubtypeDataResponseModel data;

}
