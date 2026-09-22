package com.dando.pacepilot.knowledge.service;

import com.dando.pacepilot.knowledge.chunking.TrainingDocumentChunker;
import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.loader.TrainingDocumentLoader;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingKnowledgeService {

    private final List<TrainingChunk> chunks;

    public TrainingKnowledgeService(TrainingDocumentLoader documentLoader, TrainingDocumentChunker documentChunker) {
        /*
        Loads every Markdown document.
        converts the document list into a stream.
        chunks every document.
        combines all chunk streams into one stream.
        stores the final list.
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

    public Optional<TrainingChunk> findChunkById(String chunkId) {
        return chunks.stream()
                .filter(chunk ->
                        chunk.id().equals(chunkId)
                )
                .findFirst();
    }
}