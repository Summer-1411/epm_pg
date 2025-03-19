package com.fis.epm.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallBackUrlModel implements Serializable {
	
	@JsonProperty("tranid")
	private String tranid;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("message")
	private String message;
	
}
