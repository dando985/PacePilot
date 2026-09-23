package com.dando.pacepilot.generation.ollama;

import com.dando.pacepilot.generation.TextGenerationException;
import com.dando.pacepilot.generation.TextGenerationProvider;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

public class OllamaTextGenerationProvider implements TextGenerationProvider {

    private final RestClient restClient;
    private final String model;

    public OllamaTextGenerationProvider(RestClient restClient, String model) {
        this.restClient = restClient;
        this.model = model;
    }

    @Override
    public String generate(String systemMessage, String userMessage) {
        validateMessages(systemMessage, userMessage);

        // limit number of tokens used by LLM
        int TOKENS = 300;

        OllamaChatRequest request =
                new OllamaChatRequest(
                        model,
                        List.of(
                                new OllamaChatMessage(
                                        "system",
                                        systemMessage
                                ),
                                new OllamaChatMessage(
                                        "user",
                                        userMessage
                                )
                        ),
                        false,
                        Map.of(
                                "num_predict",
                                TOKENS
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
            throw new TextGenerationException("Unable to generate text using Ollama.", exception);
        }

        if (response == null
                || response.message() == null
                || response.message().content() == null
                || response.message().content().isBlank()) {

            throw new TextGenerationException("Ollama returned no generated text.");
        }

        return response.message().content().trim();
    }

    private void validateMessages(String systemMessage, String userMessage) {
        if (systemMessage == null || systemMessage.isBlank()) {
            throw new IllegalArgumentException("System message must not be blank.");
        }

        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("User message must not be blank.");
        }
    }

    private record OllamaChatRequest(
            String model,
            List<OllamaChatMessage> messages,
            boolean stream,
            Map<String, Integer> options
    ) {
    }

    private record OllamaChatMessage(
            String role,
            String content
    ) {
    }

    private record OllamaChatResponse(
            String model,
            OllamaChatMessage message,
            boolean done
    ) {
    }
}