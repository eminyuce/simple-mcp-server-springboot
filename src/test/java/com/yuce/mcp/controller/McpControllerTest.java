package com.yuce.mcp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuce.mcp.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class McpControllerTest {

    private ToolCallbackProvider toolCallbackProvider;
    private WeatherService weatherService;
    private McpController mcpController;

    @BeforeEach
    void setUp() {
        toolCallbackProvider = mock(ToolCallbackProvider.class);
        weatherService = mock(WeatherService.class);
        mcpController = new McpController(toolCallbackProvider, weatherService);
    }

    @Test
    void testGetToolMetadata_returnsCorrectToolList() {
        ToolCallback mockCallback = mock(ToolCallback.class);
        ToolDefinition toolDef = new ToolDefinition() {
            @Override
            public String name() {
                return "getWeather";
            }

            @Override
            public String description() {
                return "Returns weather";
            }

            @Override
            public String inputSchema() {
                return "city:string";
            }
        };
        when(mockCallback.getToolDefinition()).thenReturn(toolDef);

        when(toolCallbackProvider.getToolCallbacks()).thenReturn(new ToolCallback[]{mockCallback});

        List<Map<String, String>> tools = mcpController.getToolMetadata();

        assertEquals(1, tools.size());
        assertEquals("getWeather", tools.get(0).get("name"));
        assertEquals("Returns weather", tools.get(0).get("description"));
        assertEquals("city:string", tools.get(0).get("parameters"));
    }

    @Test
    void testHandleMessage_validInput_triggersWeatherServiceAndBroadcast() {
        String city = "Istanbul";
        String jsonMessage = String.format("{\"action\":\"getWeather\",\"city\":\"%s\"}", city);

        when(weatherService.getWeather(city)).thenReturn("Sunny");

        // Call private method indirectly
        mcpController.handleMessage(jsonMessage);

        verify(weatherService, times(1)).getWeather(city);
    }

    @Test
    void testHandleMessage_invalidJson_broadcastsError() {
        String invalidJson = "invalid_json";

        assertDoesNotThrow(() -> mcpController.handleMessage(invalidJson));
    }

    @Test
    void testHandleMessage_missingActionOrCity_broadcastsError() {
        String jsonMissingCity = "{\"action\":\"getWeather\"}";
        assertDoesNotThrow(() -> mcpController.handleMessage(jsonMissingCity));

        String jsonMissingAction = "{\"city\":\"Paris\"}";
        assertDoesNotThrow(() -> mcpController.handleMessage(jsonMissingAction));
    }

    @Test
    void testStreamEvents_registersEmitterAndSendsToolList() {
        ToolCallback mockCallback = mock(ToolCallback.class);
        ToolDefinition toolDef = new ToolDefinition() {
            @Override
            public String name() {
                return "getWeather";
            }

            @Override
            public String description() {
                return "Get weather";
            }

            @Override
            public String inputSchema() {
                return "city:string";
            }
        };
        when(mockCallback.getToolDefinition()).thenReturn(toolDef);
        when(toolCallbackProvider.getToolCallbacks()).thenReturn(new ToolCallback[]{mockCallback});

        SseEmitter emitter = mcpController.streamEvents();

        assertNotNull(emitter);
    }
}
