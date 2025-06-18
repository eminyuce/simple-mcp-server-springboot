package com.yuce.mcp.model;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@RequiredArgsConstructor
public class ToolDefinition {
    private String name;
    private String description;
    private String inputSchema; // Using a Map to represent JSON Schema
    private String parameters;
    // Getters and Setters
}