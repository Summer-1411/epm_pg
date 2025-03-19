package com.fis.epm.napas.models;

import java.io.Serializable;
import java.util.List;

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
public class AutopayRiskResponseModel implements Serializable{
	@JsonProperty("gatewayCode")
	private String gatewayCode;
	
	@JsonProperty("review")
	private AutopayRiskResponseReviewModel review;
	
	@JsonProperty("rule")
	private List<AutopayRiskResponseRuleModel> rule;
}
