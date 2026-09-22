package com.dando.pacepilot.embedding.ollama;

import com.dando.pacepilot.embedding.EmbeddingException;
import com.dando.pacepilot.embedding.EmbeddingProvider;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

public class OllamaEmbeddingProvider implements EmbeddingProvider {

    private final RestClient restClient;
    private final String model;

    public OllamaEmbeddingProvider(RestClient restClient, String model) {
        this.restClient = restClient;
        this.model = model;
    }

    @Override
    public double[] createEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text to embed must not be blank.");
        }

        OllamaEmbedRequest request = new OllamaEmbedRequest(model, text);

        OllamaEmbedResponse response;

        try {
            response = restClient
                    .post()
                    .uri("/api/embed")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OllamaEmbedResponse.class);
        } catch (RestClientException exception) {
            throw new EmbeddingException("Unable to create an embedding using Ollama.", exception);
        }

        if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
            throw new EmbeddingException("Ollama returned no embeddings.");
        }

        List<Double> embedding = response.embeddings().getFirst();

        if (embedding == null || embedding.isEmpty()) {
            throw new EmbeddingException("Ollama returned an empty embedding.");
        }

        return embedding.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // helper record to build HTTP request as a JSON
    private record OllamaEmbedRequest(String model, String input) {
    }

    // helper record to convert HTTP JSON response as a Java object
    private record OllamaEmbedResponse(String model, List<List<Double>> embeddings) {
    }
}