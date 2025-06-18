package com.yuce.mcp.controller;

import com.yuce.mcp.model.PromptResponse;
import com.yuce.mcp.model.ToolDefinition;
import com.yuce.mcp.service.PromptService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping()
@Slf4j
@AllArgsConstructor
public class PromptController {

    private final PromptService promptService;

    @GetMapping(value = "/prompts",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PromptResponse>> getPrompts() {
        try {
            log.info("Fetching prompts");
            return ResponseEntity.ok(promptService.getPrompts());
        } catch (Exception e) {
            log.error("Failed to fetch prompts", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
