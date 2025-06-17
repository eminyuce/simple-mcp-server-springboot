package com.yuce.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(description = "Get weather information by city name")
    public String getWeather(@ToolParam(description = "The name of the city to get weather for") String cityName) {
        // Implementation
        return cityName+" 12 c";
    }
}