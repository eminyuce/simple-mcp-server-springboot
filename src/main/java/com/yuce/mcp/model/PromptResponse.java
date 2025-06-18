package com.yuce.mcp.model;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PromptResponse {
    private String name;
    private String content;
}
