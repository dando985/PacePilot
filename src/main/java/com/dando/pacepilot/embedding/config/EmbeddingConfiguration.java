package com.dando.pacepilot.embedding.config;

import com.dando.pacepilot.embedding.EmbeddingProvider;
import com.dando.pacepilot.embedding.ollama.OllamaEmbeddingProvider;
import com.dando.pacepilot.embedding.ollama.OllamaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class EmbeddingConfiguration {

    @Bean
    public RestClient ollamaRestClient(OllamaProperties properties) {
        return RestClient.builder().baseUrl(properties.baseUrl().toString()).build();
    }

    @Bean
    public EmbeddingProvider embeddingProvider(RestClient ollamaRestClient, OllamaProperties properties) {
        return new OllamaEmbeddingProvider(ollamaRestClient, properties.embeddingModel()
        );
    }
}