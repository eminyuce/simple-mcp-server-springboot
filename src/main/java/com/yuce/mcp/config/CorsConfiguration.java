package com.yuce.mcp.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.yuce.mcp.utils.Constants.ALLOWED_METHODS;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    private String allowedOrigin="http://localhost:8061";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods(ALLOWED_METHODS)
                .allowedHeaders("*")
                .allowCredentials(true); // If credentials are needed
    }
}