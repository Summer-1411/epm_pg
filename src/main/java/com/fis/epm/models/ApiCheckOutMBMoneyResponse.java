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
public class ApiCheckOutMBMoneyResponse {
    @JsonProperty("debug")
    private Debug debug;

    @JsonProperty("data")
    private String data;


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Debug {
        @JsonProperty("request")
        private Request request;

        @JsonProperty("response")
        private Response response;


        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Request {
            @JsonProperty("signature")
            private String signature;

            @JsonProperty("partnerCode")
            private String partnerCode;

            @JsonProperty("transactionId")
            private String transactionId;

            @JsonProperty("billCode")
            private String billCode;

            @JsonProperty("billAmount")
            private Long billAmount;

            @JsonProperty("billComment")
            private String billComment;

            @JsonProperty("redirectUrl")
            private String redirectUrl;

            @JsonProperty("callbackUrl")
            private String callbackUrl;

            @JsonProperty("productCatalogue")
            private String productCatalogue;
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Response {
            @JsonProperty("code")
            private Integer code;

            @JsonProperty("description")
            private String description;

            @JsonProperty("signature")
            private String signature;

            @JsonProperty("partnerCode")
            private String partnerCode;

            @JsonProperty("transactionId")
            private String transactionId;

            @JsonProperty("amount")
            private Long amount;

            @JsonProperty("paymentRedirectUrl")
            private String paymentRedirectUrl;
        }
    }
}
