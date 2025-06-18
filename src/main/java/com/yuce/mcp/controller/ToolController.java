package com.yuce.mcp.controller;

import com.yuce.mcp.model.ToolDefinition;
import com.yuce.mcp.model.ToolRequest;
import com.yuce.mcp.service.ToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/mcp")
@Slf4j
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolDispatcher) {
        this.toolService = toolDispatcher;
    }

    /**
     * SSE endpoint for live tool responses and initial tool list
     */
    @GetMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamEvents() {
        try {
            log.info("Opening SSE stream for /mcp/message");
            return ResponseEntity.ok(toolService.streamEvents());
        } catch (Exception e) {
            log.error("Error while opening SSE stream", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * REST endpoint to get tool metadata
     */
    @GetMapping("/tools")
    public ResponseEntity<List<ToolDefinition>> getToolMetadata() {
        try {
            log.info("Fetching tool metadata");
            var tools = toolService.getToolMetadata();
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            log.error("Failed to fetch tool metadata", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint for tool invocation via message
     */
    @PostMapping("/call")
    public ResponseEntity<Void> callTool(@RequestBody ToolRequest request) {
        try {
            log.info("Received message: {}", request);
            toolService.executeTool(request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to handle message", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
