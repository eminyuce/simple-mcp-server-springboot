package com.yuce.mcp.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ToolResponse {
    private String tool;
    private String result;
    private int httpCode;
}
