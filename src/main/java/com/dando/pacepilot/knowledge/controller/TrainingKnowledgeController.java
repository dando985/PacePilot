package com.dando.pacepilot.knowledge.controller;

import com.dando.pacepilot.knowledge.api.TrainingChunkResponse;
import com.dando.pacepilot.knowledge.service.TrainingKnowledgeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class TrainingKnowledgeController {

    private final TrainingKnowledgeService knowledgeService;

    public TrainingKnowledgeController(TrainingKnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    @GetMapping("/chunks")
    public List<TrainingChunkResponse> getAllChunks() {
        return knowledgeService
                .getAllChunks()
                .stream()
                .map(TrainingChunkResponse::from)
                .toList();
    }

    @GetMapping("/chunks/{chunkId}")
    public TrainingChunkResponse getChunkById(@PathVariable String chunkId) {
        return knowledgeService
                .findChunkById(chunkId)
                .map(TrainingChunkResponse::from)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Training chunk was not found"
                        )
                );
    }
}