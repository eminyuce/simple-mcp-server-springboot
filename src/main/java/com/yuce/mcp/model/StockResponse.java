package com.yuce.mcp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GlobalQuote {
        @JsonProperty("05. price")
        private String price;
        @JsonProperty("01. symbol")
        private String symbol;
    }
}