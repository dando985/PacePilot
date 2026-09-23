package com.dando.pacepilot.generation.ollama;

import com.dando.pacepilot.generation.TextGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaTextGenerationProviderTest {

    private MockRestServiceServer mockServer;
    private OllamaTextGenerationProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://localhost:11434");

        mockServer = MockRestServiceServer.bindTo(builder).build();

        provider = new OllamaTextGenerationProvider(builder.build(), "llama3.2:3b", 300);
    }

    @Test
    void returnsGeneratedText() {
        mockServer
                .expect(requestTo("http://localhost:11434/api/chat"))
                .andExpect(content().json("""
                        {
                          "model": "llama3.2:3b",
                          "messages": [
                            {
                              "role": "system",
                              "content": "Answer using the supplied training knowledge."
                            },
                            {
                              "role": "user",
                              "content": "How much recovery does a beginner need?"
                            }
                          ],
                          "stream": false,
                          "options": {
                            "num_predict": 300
                          }
                        }
                        """))
                .andRespond(withSuccess(
                        """
                        {
                          "model": "llama3.2:3b",
                          "message": {
                            "role": "assistant",
                            "content": "Beginners should include recovery between running sessions."
                          },
                          "done": true
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        String result = provider.generate(
                "Answer using the supplied training knowledge.",
                "How much recovery does a beginner need?"
        );

        assertThat(result).isEqualTo("Beginners should include recovery between running sessions.");

        mockServer.verify();
    }

    @Test
    void rejectsBlankUserMessage() {
        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        provider.generate(
                                "Answer using the context.",
                                " "
                        )
                )
                .withMessage("User message must not be blank.");
    }

    @Test
    void throwsExceptionWhenResponseHasNoText() {
        mockServer
                .expect(requestTo("http://localhost:11434/api/chat"))
                .andRespond(withSuccess(
                        """
                        {
                          "model": "llama3.2:3b",
                          "message": {
                            "role": "assistant",
                            "content": ""
                          },
                          "done": true
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        assertThatThrownBy(() ->
                provider.generate(
                        "Answer using the context.",
                        "How should a beginner recover?"
                )
        )
                .isInstanceOf(TextGenerationException.class)
                .hasMessage("Ollama returned no generated text.");

        mockServer.verify();
    }
}