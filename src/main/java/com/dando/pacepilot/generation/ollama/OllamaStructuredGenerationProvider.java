package com.dando.pacepilot.generation.ollama;

import com.dando.pacepilot.generation.StructuredGenerationProvider;
import com.dando.pacepilot.generation.TextGenerationException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

public class OllamaStructuredGenerationProvider implements StructuredGenerationProvider {

    private final RestClient restClient;
    private final String model;
    private final int maxOutputTokens;

    public OllamaStructuredGenerationProvider(RestClient restClient, String model, int maxOutputTokens) {
        this.restClient = restClient;
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
    }

    @Override
    public String generate(String systemMessage, String userMessage, Map<String, Object> jsonSchema) {
        validateRequest(systemMessage, userMessage, jsonSchema);

        OllamaChatRequest request = new OllamaChatRequest(
                model,
                List.of(
                        new OllamaChatMessage("system", systemMessage),
                        new OllamaChatMessage("user", userMessage)
                ),
                false,
                jsonSchema,
                Map.of(
                        "num_predict", maxOutputTokens,
                        "temperature", 0
                )
        );

        OllamaChatResponse response;

        try {
            response = restClient
                    .post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaChatResponse.class);
        } catch (RestClientException exception) {
            throw new TextGenerationException("Unable to generate structured output using Ollama.", exception);
        }

        if (response == null) {
            throw new TextGenerationException("Ollama returned no response.");
        }

        if (!response.done() || !"stop".equals(response.doneReason())) {
            throw new TextGenerationException("Ollama did not complete structured generation normally. "
                            + "Stop reason: " + response.doneReason());
        }

        if (response.message() == null || response.message().content() == null || response.message().content().isBlank()) {
            throw new TextGenerationException("Ollama returned no generated JSON.");
        }

        return response.message().content().trim();
    }

    private void validateRequest(String systemMessage, String userMessage, Map<String, Object> jsonSchema) {
        if (systemMessage == null || systemMessage.isBlank()) {
            throw new IllegalArgumentException("System message must not be blank.");
        }

        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("User message must not be blank.");
        }

        if (jsonSchema == null || jsonSchema.isEmpty()) {
            throw new IllegalArgumentException("JSON schema must not be empty.");
        }
    }

    private record OllamaChatRequest(
            String model,
            List<OllamaChatMessage> messages,
            boolean stream,
            Map<String, Object> format,
            Map<String, Integer> options
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaChatMessage(
            String role,
            String content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OllamaChatResponse(
            OllamaChatMessage message,
            boolean done,
            @JsonProperty("done_reason") String doneReason
    ) {
    }
}