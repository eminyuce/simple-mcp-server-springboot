package com.yuce.mcp.controller;

import com.yuce.mcp.service.ToolDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/mcp")
@Slf4j
public class McpController {

    private final ToolDispatcher toolDispatcher;

    public McpController(ToolDispatcher toolDispatcher) {
        this.toolDispatcher = toolDispatcher;
    }

    /**
     * SSE endpoint for live tool responses and initial tool list
     */
    @GetMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> streamEvents() {
        try {
            log.info("Opening SSE stream for /mcp/message");
            return ResponseEntity.ok(toolDispatcher.streamEvents());
        } catch (Exception e) {
            log.error("Error while opening SSE stream", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * REST endpoint to get tool metadata
     */
    @GetMapping("/tools")
    public ResponseEntity<List<Map<String, String>>> getToolMetadata() {
        try {
            log.info("Fetching tool metadata");
            List<Map<String, String>> tools = toolDispatcher.getToolMetadata();
            return ResponseEntity.ok(tools);
        } catch (Exception e) {
            log.error("Failed to fetch tool metadata", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint for tool invocation via message
     */
    @PostMapping("/message")
    public ResponseEntity<Void> handleMessage(@RequestBody String message) {
        try {
            log.info("Received message: {}", message);
            toolDispatcher.handleMessage(message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to handle message", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
