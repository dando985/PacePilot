package com.dando.pacepilot.knowledge.service;

import com.dando.pacepilot.knowledge.chunking.MarkdownSectionChunker;
import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import com.dando.pacepilot.knowledge.exception.TrainingDocumentLoadingException;
import com.dando.pacepilot.knowledge.loader.TrainingDocumentLoader;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class TrainingKnowledgeService {

    private final List<TrainingChunk> chunks;

    public TrainingKnowledgeService(List<TrainingDocumentLoader> documentLoaders, MarkdownSectionChunker documentChunker) {
        /*
        Loads all document types using all loader implemented by TrainingDocumentLoader
        Sorts them by source and ID for consistent ordering
        chunks every document
        combines all chunks into a single immutable list
         */
        this.chunks = documentLoaders.stream()
                .flatMap(loader -> loader.loadDocuments().stream())
                .sorted(Comparator.comparing(TrainingDocument::source).thenComparing(TrainingDocument::id))
                .flatMap(document -> documentChunker.createChunks(document).stream())
                .toList();

        if (chunks.isEmpty()) {
            throw new TrainingDocumentLoadingException(
                    "No usable training content was found. Add Markdown or PDF documents to src/main/resources/knowledge/."
            );
        }
    }

    public List<TrainingChunk> getAllChunks() {
        return chunks;
    }
}