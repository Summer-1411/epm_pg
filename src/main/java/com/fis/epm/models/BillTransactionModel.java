package com.fis.epm.models;

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
public class BillTransactionModel {
	@JsonProperty("pay_no")
	private String pay_no;
	
	@JsonProperty("amount")
	private Long amount;
	
	@JsonProperty("PhoneNumber")
	private String PhoneNumber;
	
	@JsonProperty("bankCode")
	private String bankCode;
	
	@JsonProperty("bankcode")
	private String bankcode;
	
	@JsonProperty("cardScheme")
	private String cardScheme;
	
	@JsonProperty("vnp_OrderInfo")
	private String vnp_OrderInfo;
}
