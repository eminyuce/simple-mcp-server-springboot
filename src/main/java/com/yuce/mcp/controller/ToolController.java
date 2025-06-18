package com.yuce.mcp.controller;

import com.yuce.mcp.model.ToolDefinition;
import com.yuce.mcp.model.ToolRequest;
import com.yuce.mcp.model.ToolResponse;
import com.yuce.mcp.service.ToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mcp")
@Slf4j
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    /**
     * REST endpoint to get tool metadata
     */
    @GetMapping(value = "/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ToolDefinition>> getToolMetadata(@RequestParam String format) {
        try {
            log.info("Fetching tool metadata");
            var tools = toolService.getToolMetadata(format);
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            log.error("Failed to fetch tool metadata", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint for tool invocation via message
     */
    @PostMapping(value = "/call", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ToolResponse> callTool(@RequestBody ToolRequest request) {
        try {
            log.info("Received message: {}", request);
           ;
            return ResponseEntity.ok( toolService.executeTool(request));
        } catch (Exception e) {
            log.error("Failed to handle message", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
