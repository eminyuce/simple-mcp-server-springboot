package com.yuce.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuce.mcp.repo.AuthorRepository;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ToolDispatcher {

    private final ToolCallbackProvider toolCallbackProvider;
    private final WeatherService weatherService;
    private final AuthorRepository authorRepository;
    private final ObjectMapper objectMapper;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public ToolDispatcher(ToolCallbackProvider toolCallbackProvider,
                          WeatherService weatherService,
                          AuthorRepository authorRepository,
                          ObjectMapper objectMapper) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.weatherService = weatherService;
        this.authorRepository=authorRepository;
        this.objectMapper = objectMapper;
    }

    public void handleMessage(String message) {
        try {
            var node = objectMapper.readTree(message);
            String action = node.path("action").asText(null);

            if (action == null) {
                broadcastError("Missing 'action' field.");
                return;
            }

            switch (action) {
                case "getWeather" -> handleGetWeather(node);
                case "getTopAuthors" -> handleGetTopAuthors();
                case "getAuthorByArticleTitle" -> handleGetAuthorByArticleTitle(node);
                default -> broadcastError("Unsupported action: " + action);
            }
        } catch (Exception e) {
            broadcastError("Error processing message: " + e.getMessage());
        }
    }

    private void handleGetWeather(JsonNode node) throws IOException {
        String city = node.path("city").asText(null);
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

    private void handleGetAuthorByArticleTitle(JsonNode node) throws IOException {
        String articleTitle = node.path("articleTitle").asText(null);
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

    public List<Map<String, String>> getToolMetadata() {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(tool -> Map.of(
                        "name", tool.getToolDefinition().name(),
                        "description", tool.getToolDefinition().description(),
                        "parameters", tool.getToolDefinition().inputSchema()
                ))
                .collect(Collectors.toList());
    }
}
