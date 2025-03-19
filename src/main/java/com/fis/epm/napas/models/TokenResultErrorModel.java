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
public class TokenResultErrorModel implements Serializable {

    @JsonProperty("cause")
    private String cause;

    @JsonProperty("explanation")
    private String explanation;

    @JsonProperty("supportCode")
    private String supportCode;
}
