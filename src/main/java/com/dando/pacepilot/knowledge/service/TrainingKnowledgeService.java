package com.dando.pacepilot.knowledge.service;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.chunking.MarkdownSectionChunker;
import com.dando.pacepilot.knowledge.loader.DocumentLoader;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingKnowledgeService {

    private final List<TrainingChunk> chunks;

    public TrainingKnowledgeService(DocumentLoader documentLoader, MarkdownSectionChunker documentChunker) {
        /*
        Loads every Markdown document.
        converts the document list into a stream.
        chunks every document.
        combines all chunks into a single list.
         */
        this.chunks = documentLoader
                .loadDocuments()
                .stream()
                .flatMap(document -> documentChunker.createChunks(document).stream())
                .toList();
    }

    public List<TrainingChunk> getAllChunks() {
        return chunks;
    }
}