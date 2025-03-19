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
public class TokenResultCard implements Serializable{
	
	@JsonProperty("brand")
	private String brand;
	
	@JsonProperty("nameOnCard")
	private String nameOnCard;
	
	@JsonProperty("issueDate")
	private String issueDate;
	
	@JsonProperty("number")
	private String number;
	
	@JsonProperty("scheme")
	private String scheme;
	
	@JsonProperty("expiry")
	private TokenResultCardExpiry expiry;

}
