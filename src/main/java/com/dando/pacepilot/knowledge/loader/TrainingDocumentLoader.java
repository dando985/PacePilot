package com.dando.pacepilot.knowledge.loader;

import com.dando.pacepilot.knowledge.domain.TrainingDocument;

import java.util.List;

public interface TrainingDocumentLoader {

    /**
     * Loads the documents supported by this loader.
     * Returns an empty list when no matching documents exist.
     */
    List<TrainingDocument> loadDocuments();
}