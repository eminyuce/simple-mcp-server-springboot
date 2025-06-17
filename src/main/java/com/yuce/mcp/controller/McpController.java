package com.yuce.mcp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuce.mcp.service.WeatherService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final ToolCallbackProvider toolCallbackProvider;
    private final WeatherService weatherService;
    private final ObjectMapper objectMapper;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public McpController(ToolCallbackProvider toolCallbackProvider, WeatherService weatherService) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.weatherService = weatherService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * SSE endpoint for live tool responses and initial tool list
     */
    @GetMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        SseEmitter emitter = new SseEmitter(0L); // Set timeout to 0 (no timeout)
        emitters.add(emitter);

        // Clean up on completion or timeout
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError((throwable) -> {
            emitters.remove(emitter);
            emitter.completeWithError(throwable);
        });

        try {
            // Send initial tool list
            List<Map<String, String>> tools = getToolMetadata();

            String json = objectMapper.writeValueAsString(Map.of("tools", tools));
            emitter.send(SseEmitter.event()
                    .name("tool-list")
                    .data(json));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * REST endpoint to get tool metadata
     */
    @GetMapping("/tools")
    public List<Map<String, String>> getToolMetadata() {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(tool -> Map.of(
                        "name", tool.getToolDefinition().name(),
                        "description", tool.getToolDefinition().description(),
                        "parameters", tool.getToolDefinition().inputSchema()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Endpoint for tool invocation via message
     */
    @PostMapping("/message")
    public void handleMessage(@RequestBody String message) {
        try {
            var node = objectMapper.readTree(message);
            String action = node.has("action") ? node.get("action").asText() : null;
            String city = node.has("city") ? node.get("city").asText() : null;

            if ("getWeather".equals(action) && city != null && !city.trim().isEmpty()) {
                String result = weatherService.getWeather(city);
                broadcastToolResponse(action, result);
            } else {
                broadcastError("Invalid request: action or city missing or invalid");
            }
        } catch (Exception e) {
            broadcastError("Error processing message: " + e.getMessage());
        }
    }

    private void broadcastToolResponse(String tool, String result) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("tool-response")
                        .data(objectMapper.writeValueAsString(Map.of("tool", tool, "result", result))));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    private void broadcastError(String errorMessage) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(objectMapper.writeValueAsString(Map.of("error", errorMessage))));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}