package com.dando.pacepilot.knowledge.loader;

import com.dando.pacepilot.knowledge.domain.TrainingDocument;

import java.util.List;

public interface TrainingDocumentLoader {

    List<TrainingDocument> loadDocuments();
}