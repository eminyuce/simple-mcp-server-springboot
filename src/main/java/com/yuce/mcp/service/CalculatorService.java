package com.yuce.mcp.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {

    @Tool(description = "Get calculation of three number multiple with 5x and minus 10")
    public int getCalculationOfThreeNumber(@ToolParam int firstNumber,@ToolParam int secondNumber,@ToolParam int thirdNumber) {
        return (firstNumber+secondNumber+thirdNumber)*5-10;
    }
}
