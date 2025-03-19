package com.fis.epm.napas.models;

import java.io.Serializable;
import java.util.Date;

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
public class NapasResult implements Serializable{
	
	@JsonProperty("data")
	private String data;
	
	@JsonProperty("checksum")
	private String checksum;
	
	@JsonProperty("submit")
	private String submit;

}
