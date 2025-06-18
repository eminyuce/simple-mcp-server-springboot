### Spring AI – Model Context Protocol (MCP) and MCP Server Boot Starters

Spring AI introduces **Model Context Protocol (MCP)** as a standardized way for Large Language Models (LLMs) to **interact with external data, tools, and services**. MCP provides a flexible framework that makes it easy to build intelligent agent applications in Spring Boot.

---

## 🔷 What is MCP (Model Context Protocol)?

**MCP** is a protocol and runtime model introduced by Spring AI to:

* Describe **external tools**, **functions**, or **data** that LLMs can access.
* Enable **structured communication** between the LLM and your application (e.g., call external APIs or database queries).
* Allow **bi-directional interaction** with LLMs in a standardized way.

It supports:

* **Tool usage** (function calling)
* **Retrieval-Augmented Generation (RAG)** contexts
* **Streaming responses**
* **Plugin architecture**

---

## 🔧 MCP Server Boot Starters

Spring AI offers **MCP Server Boot Starters** to help developers quickly build MCP-compliant applications. The starters include auto-configuration and built-in components.

### 📦 Key Starter Modules

| Starter                                | Description                                                                                  |
| -------------------------------------- | -------------------------------------------------------------------------------------------- |
| `spring-ai-starter-mcp-server`         | Core support for MCP server implementation.                                                  |
| `spring-ai-starter-mcp-server-webmvc`  | Adds support for HTTP transport via Spring Web MVC (serves `/mcp/chat`, `/mcp/tools`, etc.). |
| `spring-ai-starter-mcp-server-webflux` | Similar to WebMVC but for reactive applications using WebFlux.                               |

---

## 📁 Example Maven Dependencies

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>0.8.1</version>
</dependency>
```

You can also use `spring-ai-starter-mcp-server-webflux` if you're building a reactive server.

---

## 🛠️ Basic Configuration Example

```java
@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider tools(MyService myService) {
        return MethodToolCallbackProvider.of(myService);
    }
}
```

This exposes `MyService` methods as callable tools via `/mcp/tools`.

---

## 🌐 Available Endpoints (via WebMVC/WebFlux)

| Endpoint       | Purpose                                           |
| -------------- | ------------------------------------------------- |
| `/mcp/chat`    | Accepts LLM chat requests and returns results.    |
| `/mcp/tools`   | Lists the tools available to the LLM.             |
| `/mcp/context` | Describes available RAG documents and embeddings. |

---

## 📘 Use Case: Tool-Using LLM

You can build an LLM app that:

* Accepts questions like “What’s the weather in Istanbul?”
* Internally maps it to a tool (e.g., `WeatherService.getWeather(String city)`)
* Invokes it and feeds the result back to the LLM for further response generation.

---

## 🔗 Resources

* Spring AI Docs: [https://docs.spring.io/spring-ai/reference/](https://docs.spring.io/spring-ai/reference/)
* MCP GitHub Code: [https://github.com/spring-projects/spring-ai/tree/main/spring-ai-mcp](https://github.com/spring-projects/spring-ai/tree/main/spring-ai-mcp)
* Example with Ollama + MCP Server: [https://github.com/spring-projects/spring-ai/tree/main/spring-ai-examples/spring-ai-ollama-chat-mcp](https://github.com/spring-projects/spring-ai/tree/main/spring-ai-examples/spring-ai-ollama-chat-mcp)


