package com.yuce.mcp.service;

import com.yuce.mcp.model.PromptResponse;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class PromptService {

    private final ResourceLoader resourceLoader;

    public PromptService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<PromptResponse> getPrompts() throws IOException {
        return List.of(new PromptResponse("mcp-tool-prompt",readPrompt("mcp-system-prompt.st")),
                new PromptResponse("final-response-prompt",readPrompt("user-final-response.st")));
    }

    public String readPrompt(String fileName) throws IOException {
        var resource = resourceLoader.getResource("classpath:prompts/"+fileName);
        var path = resource.getFile().toPath();
        return Files.readString(path);
    }
}
