package com.yuce.mcp.config;

import com.yuce.mcp.repo.AuthorRepository;
import com.yuce.mcp.service.WeatherService;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ToolConfig {



    @Bean
    public ToolCallbackProvider tools(AuthorRepository authorRepository,
                                      WeatherService weatherService) {
        return MethodToolCallbackProvider.builder().toolObjects(authorRepository,weatherService).build();
    }
}