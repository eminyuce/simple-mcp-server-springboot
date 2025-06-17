package com.yuce.mcp.config;

import com.yuce.mcp.repo.AuthorRepository;
import com.yuce.mcp.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ToolConfigTest {

    private AuthorRepository authorRepository;
    private WeatherService weatherService;
    private ToolConfig toolConfig;

    @BeforeEach
    void setUp() {
        authorRepository = mock(AuthorRepository.class);
        weatherService = mock(WeatherService.class);
        toolConfig = new ToolConfig();
    }

    @Test
    void testToolsBeanCreation() {
        ToolCallbackProvider provider = toolConfig.tools(authorRepository, weatherService);

        assertNotNull(provider, "ToolCallbackProvider should not be null");

        // Optionally verify that it's a MethodToolCallbackProvider
        assertEquals("MethodToolCallbackProvider", provider.getClass().getSimpleName());
    }
}