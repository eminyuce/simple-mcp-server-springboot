package com.yuce.mcp.repo;

import com.yuce.mcp.model.Author;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorRepository {

    @Tool(description = "Get Baeldung author details using an article title")
    public Author getAuthorByArticleTitle(String articleTitle) {
        return new Author("John Doe", "john.doe@baeldung.com");
    }

    @Tool(description = "Get highest rated Baeldung authors")
    public List<Author> getTopAuthors() {
        return List.of(
                new Author("John Doe", "john.doe@baeldung.com"),
                new Author("Jane Doe", "jane.doe@baeldung.com")
        );
    }


}
