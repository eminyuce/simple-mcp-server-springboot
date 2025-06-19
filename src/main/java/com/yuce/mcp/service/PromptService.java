package com.yuce.mcp.service;

import com.yuce.mcp.model.PromptResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PromptService {

    private final ResourcePatternResolver resourcePatternResolver;
    private final ConcurrentMap<String, PromptResponse> promptsCache = new ConcurrentHashMap<>();
    private long lastModified = 0;

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
        // Check if the file has been modified
        if (hasFileBeenModified()) {
            reloadPrompts();
        }
        return new ArrayList<>(promptsCache.values());
    }

    /**
     * Reloads the prompts from the file.
     *
     * @throws IOException if an I/O error occurs.
     */
    private void reloadPrompts() throws IOException {
        promptsCache.clear();
        Resource[] resources = resourcePatternResolver.getResources("classpath:prompts/*.*");
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename!= null) {
                String promptName = removeFileExtension(filename);
                String promptContent = readContentFromResource(resource);
                promptsCache.put(promptName, new PromptResponse(promptName, promptContent));
            }
        }
        lastModified = System.currentTimeMillis();
    }

    /**
     * Checks if the file has been modified since the last reload.
     *
     * @return True if the file has been modified, false otherwise.
     */
    private boolean hasFileBeenModified() {
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:prompts/*.*");
            for (Resource resource : resources) {
                File file = resource.getFile();
                if (file.lastModified() > lastModified) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Handle exception
        }
        return false;
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

    @Scheduled(fixedDelay = 10000) // Check every 10 seconds
    private void scheduledReload() throws IOException {
        if (hasFileBeenModified()) {
            reloadPrompts();
        }
    }
}