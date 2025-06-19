package com.yuce.mcp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuce.mcp.model.ToolDefinition;
import com.yuce.mcp.model.ToolRequest;
import com.yuce.mcp.model.ToolResponse;
import com.yuce.mcp.repo.AuthorRepository;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
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

    public ToolService(ToolCallbackProvider toolCallbackProvider,
                       WeatherService weatherService,
                       AuthorRepository authorRepository,
                       ObjectMapper objectMapper) {
        this.toolCallbackProvider = toolCallbackProvider;
        this.weatherService = weatherService;
        this.authorRepository=authorRepository;
        this.objectMapper = objectMapper;
    }

    public ToolResponse executeTool(ToolRequest toolRequest) {
        try {
            String action = toolRequest.getTool();
            if (action == null || action.isBlank()) {
                return broadcastError("Missing or empty 'action' field.");
            }
            // Normalize action string to avoid case issues if needed
            switch (action.trim()) {
                case "getWeather":
                    return handleGetWeather(toolRequest);
                case "getTopAuthors":
                    return handleGetTopAuthors();
                case "getAuthorByArticleTitle":
                    return handleGetAuthorByArticleTitle(toolRequest);
                default:
                    return broadcastError("Unsupported action: " + action);
            }
        } catch (Exception e) {
            return broadcastError("Error processing message: " + e.getMessage());
        }
    }

    private ToolResponse handleGetWeather(ToolRequest toolRequest) throws IOException {
        String city = toolRequest.getParameters().get("cityName");
        if (city == null) {
            return  broadcastError("Missing 'city' field for getWeather action.");
        }
        String result = weatherService.getWeather(city);
        return  broadcastToolResponse("getWeather", result);
    }

    private ToolResponse handleGetTopAuthors() throws IOException {
        String result = objectMapper.writeValueAsString(authorRepository.getTopAuthors());
        return  broadcastToolResponse("getTopAuthors", result);
    }

    private ToolResponse handleGetAuthorByArticleTitle(ToolRequest toolRequest) throws IOException {
        String articleTitle = toolRequest.getParameters().get("articleTitle");
        if (articleTitle == null) {
            return broadcastError("Missing 'articleTitle' field for getAuthorByArticleTitle action.");
        }
        String result = objectMapper.writeValueAsString(authorRepository.getAuthorByArticleTitle(articleTitle));
        return broadcastToolResponse("getAuthorByArticleTitle", result);
    }


    private ToolResponse broadcastToolResponse(String tool, String result) {
        return new ToolResponse(tool,result, HttpStatus.OK.value());
    }

    private ToolResponse broadcastError(String errorMessage) {
        return new ToolResponse("",errorMessage, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Cacheable(value = "tools", key = "#format != null ? #format.toLowerCase() : 'default'")
    public List<ToolDefinition> getToolMetadata(String format) {
        boolean isOpenAI = "openai".equalsIgnoreCase(format);

        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(tool -> {
                    ToolDefinition definition = new ToolDefinition();
                    definition.setName(tool.getToolDefinition().name());
                    definition.setDescription(tool.getToolDefinition().description());
                    String inputSchema = tool.getToolDefinition().inputSchema();

                    if (isOpenAI) {
                        try {
                            // Convert JSON string into a structured map (so it's not returned as a string)
                            Map<String, Object> parameters = objectMapper.readValue(inputSchema, new TypeReference<>() {});
                            definition.setParameters(parameters);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse inputSchema for tool: " + tool.getToolDefinition().name(), e);
                        }
                    } else {
                        definition.setInputSchema(inputSchema); // raw JSON string
                    }

                    return definition;
                })
                .collect(Collectors.toList());
    }
}
