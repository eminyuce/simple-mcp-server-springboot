package com.yuce.mcp.model;


import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {
    private Main main;
    private String name;

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Main {
        private double temp;
        private int humidity;
    }

}