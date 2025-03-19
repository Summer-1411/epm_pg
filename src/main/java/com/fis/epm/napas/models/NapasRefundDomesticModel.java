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
public class NapasRefundDomesticModel implements Serializable {
    @JsonProperty("apiOperation")
    private String apiOperation;

    @JsonProperty("transaction")
    private NapasRefundDomesticTransactionModel transaction;

    @JsonProperty("channel")
    private String channel;
}
