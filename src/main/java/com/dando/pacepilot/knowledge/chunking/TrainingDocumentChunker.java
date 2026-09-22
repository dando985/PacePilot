package com.dando.pacepilot.knowledge.chunking;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.domain.TrainingDocument;

import java.util.List;

public interface TrainingDocumentChunker {

    List<TrainingChunk> createChunks(TrainingDocument document);
}