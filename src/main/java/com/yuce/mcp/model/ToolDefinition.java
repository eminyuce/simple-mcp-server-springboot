package com.yuce.mcp.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ToolDefinition {
    private String name;
    private String description;
    private String inputSchema; // Using a Map to represent JSON Schema
    private Map<String, Object> parameters;
    // Getters and Setters
}