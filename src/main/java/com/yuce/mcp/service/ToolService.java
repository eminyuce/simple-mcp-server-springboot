package com.yuce.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuce.mcp.model.ToolDefinition;
import com.yuce.mcp.model.ToolRequest;
import com.yuce.mcp.repo.AuthorRepository;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ToolService {

    private final ToolCallbackProvider toolCallbackProvider;
    private final WeatherService weatherService;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public ToolService(ToolCallbackProvider toolCallbackProvider,
                       WeatherService weatherService,
                       AuthorRepository authorRepository,
                       ObjectMapper objectMapper) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.weatherService = weatherService;
        this.authorRepository=authorRepository;
        this.objectMapper = objectMapper;
    }

    public void executeTool(ToolRequest toolRequest) {
        try {
            String action = toolRequest.getTool();

            if (action == null) {
                broadcastError("Missing 'action' field.");
                return;
            }

            switch (action) {
                case "getWeather" -> handleGetWeather(toolRequest);
                case "getTopAuthors" -> handleGetTopAuthors();
                case "getAuthorByArticleTitle" -> handleGetAuthorByArticleTitle(toolRequest);
                default -> broadcastError("Unsupported action: " + action);
            }
        } catch (Exception e) {
            broadcastError("Error processing message: " + e.getMessage());
        }
    }

    private void handleGetWeather(ToolRequest toolRequest) throws IOException {
        String city = toolRequest.getParameters().get("city");
        if (city == null) {
            broadcastError("Missing 'city' field for getWeather action.");
            return;
        }
        String result = weatherService.getWeather(city);
        broadcastToolResponse("getWeather", result);
    }

    private void handleGetTopAuthors() throws IOException {
        String result = objectMapper.writeValueAsString(authorRepository.getTopAuthors());
        broadcastToolResponse("getTopAuthors", result);
    }

    private void handleGetAuthorByArticleTitle(ToolRequest toolRequest) throws IOException {
        String articleTitle = toolRequest.getParameters().get("articleTitle");
        if (articleTitle == null) {
            broadcastError("Missing 'articleTitle' field for getAuthorByArticleTitle action.");
            return;
        }
        String result = objectMapper.writeValueAsString(authorRepository.getAuthorByArticleTitle(articleTitle));
        broadcastToolResponse("getAuthorByArticleTitle", result);
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
            var tools = getToolMetadata();

            String json = objectMapper.writeValueAsString(Map.of("tools", tools));
            emitter.send(SseEmitter.event()
                    .name("tool-list")
                    .data(json));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
    @Cacheable("tools")
    public List<ToolDefinition> getToolMetadata() {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(tool -> {
                    ToolDefinition definition = new ToolDefinition();
                    definition.setName(tool.getToolDefinition().name());
                    definition.setDescription(tool.getToolDefinition().description());
                    definition.setInputSchema(tool.getToolDefinition().inputSchema());
                    return definition;
                })
                .collect(Collectors.toList());
    }
}
