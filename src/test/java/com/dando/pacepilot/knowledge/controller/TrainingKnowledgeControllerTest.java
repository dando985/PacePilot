package com.dando.pacepilot.knowledge.controller;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.service.TrainingKnowledgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingKnowledgeController.class)
class TrainingKnowledgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TrainingKnowledgeService knowledgeService;

    @Test
    void returnsAllChunks() throws Exception {
        TrainingChunk chunk = createChunk();

        when(knowledgeService.getAllChunks()).thenReturn(List.of(chunk));

        mockMvc.perform(get("/api/knowledge/chunks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("beginner-running-foundations-chunk-2"))
                .andExpect(jsonPath("$[0].documentTitle").value("Beginner Running Foundations"))
                .andExpect(jsonPath("$[0].sectionTitle").value("Starting with Run-Walk Sessions"))
                .andExpect(jsonPath("$[0].chunkNumber").value(2));

        verify(knowledgeService).getAllChunks();
    }

    @Test
    void returnsChunkById() throws Exception {
        TrainingChunk chunk = createChunk();

        when(knowledgeService.findChunkById(chunk.id())).thenReturn(Optional.of(chunk));

        mockMvc.perform(
                        get(
                                "/api/knowledge/chunks/{chunkId}",
                                chunk.id()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(chunk.id()))
                .andExpect(jsonPath("$.content").value(chunk.content()));

        verify(knowledgeService).findChunkById(chunk.id());
    }

    @Test
    void returnsNotFoundForUnknownChunk() throws Exception {
        String unknownId = "unknown-chunk";

        when(knowledgeService.findChunkById(unknownId)).thenReturn(Optional.empty());

        mockMvc.perform(
                        get(
                                "/api/knowledge/chunks/{chunkId}",
                                unknownId
                        )
                )
                .andExpect(status().isNotFound());

        verify(knowledgeService).findChunkById(unknownId);
    }

    // helper to create sample chunk
    private TrainingChunk createChunk() {
        return new TrainingChunk(
                "beginner-running-foundations-chunk-2",
                "beginner-running-foundations",
                "Beginner Running Foundations",
                "knowledge/beginner-running-foundations.md",
                "Starting with Run-Walk Sessions",
                2,
                "A beginner can alternate running and walking."
        );
    }
}