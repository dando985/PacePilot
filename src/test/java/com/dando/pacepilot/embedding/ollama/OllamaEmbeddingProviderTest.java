package com.dando.pacepilot.embedding.ollama;

import com.dando.pacepilot.embedding.EmbeddingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaEmbeddingProviderTest {

    private MockRestServiceServer mockServer;
    private OllamaEmbeddingProvider embeddingProvider;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl("http://localhost:11434");

        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

        RestClient restClient = restClientBuilder.build();

        embeddingProvider = new OllamaEmbeddingProvider(restClient, "embeddinggemma");
    }

    @Test
    void createsEmbeddingFromOllamaResponse() {
        String text = "Easy running builds aerobic fitness.";

        mockServer
                .expect(requestTo("http://localhost:11434/api/embed"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "model": "embeddinggemma",
                          "input": "Easy running builds aerobic fitness."
                        }
                        """))
                .andRespond(withSuccess(
                        """
                        {
                          "model": "embeddinggemma",
                          "embeddings": [
                            [0.12, -0.45, 0.89]
                          ]
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        double[] embedding = embeddingProvider.createEmbedding(text);

        assertThat(embedding).containsExactly(0.12, -0.45, 0.89);

        mockServer.verify();
    }

    @Test
    void rejectsBlankText() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> embeddingProvider.createEmbedding(" "))
                .withMessage("Text to embed must not be blank.");
    }

    @Test
    void throwsExceptionWhenOllamaReturnsNoEmbeddings() {
        mockServer
                .expect(requestTo("http://localhost:11434/api/embed"))
                .andRespond(withSuccess(
                        """
                        {
                          "model": "embeddinggemma",
                          "embeddings": []
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() -> embeddingProvider.createEmbedding("Recovery is part of training."))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("Ollama returned no embeddings.");

        mockServer.verify();
    }

    @Test
    void convertsHttpFailureToEmbeddingException() {
        mockServer
                .expect(requestTo("http://localhost:11434/api/embed"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> embeddingProvider.createEmbedding("Recovery is part of training."))
                .isInstanceOf(EmbeddingException.class)
                .hasMessage("Unable to create an embedding using Ollama.")
                .hasCauseInstanceOf(RestClientException.class);

        mockServer.verify();
    }
}