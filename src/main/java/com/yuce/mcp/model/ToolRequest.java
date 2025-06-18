package com.yuce.mcp.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ToolRequest {
    private String tool;
    private Map<String, String> parameters;
}
