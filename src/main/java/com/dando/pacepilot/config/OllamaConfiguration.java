package com.dando.pacepilot.config;

import com.dando.pacepilot.embedding.EmbeddingProvider;
import com.dando.pacepilot.embedding.ollama.OllamaEmbeddingProvider;
import com.dando.pacepilot.generation.TextGenerationProvider;
import com.dando.pacepilot.generation.ollama.OllamaTextGenerationProvider;
import com.dando.pacepilot.ollama.OllamaProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OllamaProperties.class)
public class OllamaConfiguration {

    @Bean
    public RestClient ollamaRestClient(OllamaProperties properties) {
        return RestClient.builder().baseUrl(properties.baseUrl().toString()).build();
    }

    @Bean
    public EmbeddingProvider embeddingProvider(@Qualifier("ollamaRestClient") RestClient restClient, OllamaProperties properties) {
        return new OllamaEmbeddingProvider(restClient, properties.embeddingModel());
    }

    @Bean
    public TextGenerationProvider textGenerationProvider(@Qualifier("ollamaRestClient") RestClient restClient, OllamaProperties properties) {
        return new OllamaTextGenerationProvider(restClient, properties.chatModel(), properties.maxOutputTokens());
    }
}