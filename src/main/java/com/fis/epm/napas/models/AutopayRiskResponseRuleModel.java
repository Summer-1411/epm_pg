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
public class AutopayRiskResponseRuleModel implements Serializable{
	@JsonProperty("data")
	private String data;
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("recommendation")
	private String recommendation;
	
	@JsonProperty("type")
	private String type;
}
