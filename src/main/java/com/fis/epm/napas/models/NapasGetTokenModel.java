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
public class NapasGetTokenModel implements Serializable {

    @JsonProperty("status")
    private String status;

    @JsonProperty("merchantId")
    private String merchantId;

    @JsonProperty("result")
    private String result;

    @JsonProperty("token")
    private String token;

    @JsonProperty("domestic")
    private String domestic;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("error")
    private TokenResultErrorModel error;
}
