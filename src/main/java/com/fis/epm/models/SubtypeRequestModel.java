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
public class SubtypeRequestModel implements Serializable {
	
	@JsonProperty("p_strsdt")
	private String p_strsdt;
	
}
