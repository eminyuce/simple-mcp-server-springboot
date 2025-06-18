package com.yuce.mcp.service;

import com.yuce.mcp.model.PromptResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class PromptService {

    private final ResourcePatternResolver resourcePatternResolver;

    // Inject ResourcePatternResolver instead of ResourceLoader
    public PromptService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * Dynamically loads all prompts from the 'classpath:prompts/' directory.
     * The name of the prompt is derived from its filename, excluding the extension.
     *
     * @return A list of PromptResponse objects.
     * @throws IOException if the resources cannot be read.
     */
    public List<PromptResponse> getPrompts() throws IOException {
        List<PromptResponse> prompts = new ArrayList<>();

        // Use a pattern to find all files in the prompts directory
        Resource[] resources = resourcePatternResolver.getResources("classpath:prompts/*.*");

        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null) {
                // Use the filename (without extension) as the attribute name
                String promptName = removeFileExtension(filename);
                String promptContent = readContentFromResource(resource);
                prompts.add(new PromptResponse(promptName, promptContent));
            }
        }
        return prompts;
    }

    /**
     * Reads the content of a resource into a String.
     * This method correctly handles resources within a JAR file.
     *
     * @param resource The resource to read from.
     * @return The content of the file as a String.
     * @throws IOException if an I/O error occurs.
     */
    private String readContentFromResource(Resource resource) throws IOException {
        try (var inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Removes the file extension from a filename.
     * e.g., "my-prompt.st" becomes "my-prompt".
     *
     * @param filename The original filename.
     * @return The filename without its extension.
     */
    private String removeFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return filename; // No extension found
        }
        return filename.substring(0, lastIndexOfDot);
    }
}