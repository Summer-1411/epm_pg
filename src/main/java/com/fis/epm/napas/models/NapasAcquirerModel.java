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
public class NapasAcquirerModel implements Serializable {

	@JsonProperty("batch")
	private String batch;

	@JsonProperty("date")
	private String date;

	@JsonProperty("id")
	private String id;

	@JsonProperty("merchantId")
	private String merchantId;

	@JsonProperty("transactionId")
	private String transactionId;
}
