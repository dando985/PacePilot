package com.dando.pacepilot.knowledge.loader;

import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClasspathTrainingDocumentLoaderTest {

    private final ClasspathTrainingDocumentLoader documentLoader = new ClasspathTrainingDocumentLoader();

    @Test
    void loadsAllMarkdownDocuments() {
        List<TrainingDocument> documents = documentLoader.loadDocuments();

        assertThat(documents)
                .extracting(TrainingDocument::id)
                .contains(
                        "beginner-running-foundations",
                        "general-fitness-foundations"
                );
    }

    @Test
    void extractsDocumentTitleAndContent() {
        List<TrainingDocument> documents = documentLoader.loadDocuments();

        TrainingDocument runningDocument = documents.stream()
                .filter(document ->
                        document.id().equals("beginner-running-foundations")
                )
                .findFirst()
                .orElseThrow();

        assertThat(runningDocument.title()).isEqualTo("Beginner Running Foundations");

        assertThat(runningDocument.source()).isEqualTo("knowledge/beginner-running-foundations.md");

        assertThat(runningDocument.content()).contains("## Starting with Run-Walk Sessions");
    }
}