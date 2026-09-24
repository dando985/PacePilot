package com.dando.pacepilot.knowledge.loader;

import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import com.dando.pacepilot.knowledge.exception.TrainingDocumentLoadingException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
public class DocumentLoader {

    private static final String RESOURCE_PATTERN = "classpath*:knowledge/*.md";

    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    public List<TrainingDocument> loadDocuments() {
        try {
            Resource[] resources = resourceResolver.getResources(RESOURCE_PATTERN);

            if (resources.length == 0) {
                throw new TrainingDocumentLoadingException("No training documents were found");
            }

            Arrays.sort(resources, Comparator.comparing(this::getSortableFilename));

            List<TrainingDocument> documents = new ArrayList<>();

            for (Resource resource : resources) {
                documents.add(loadDocument(resource));
            }

            return List.copyOf(documents);

        } catch (IOException exception) {
            throw new TrainingDocumentLoadingException("Unable to load training documents", exception);
        }
    }

    // helper to extract document metadata from a resource object
    private TrainingDocument loadDocument(Resource resource) throws IOException {
        String filename = resource.getFilename();

        if (filename == null) {
            throw new IOException("A training resource has no filename");
        }

        String content = readContent(resource);
        String documentId = removeMarkdownExtension(filename);
        String title = extractTitle(content, documentId);
        String source = "knowledge/" + filename;

        return new TrainingDocument(
                documentId,
                title,
                source,
                content
        );
    }

    // helper to extract the string contents from a resource object
    private String readContent(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {

            byte[] bytes = inputStream.readAllBytes();

            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    // helper to remove .md extension from file name
    private String removeMarkdownExtension(String filename) {
        return filename.substring(0, filename.length() - ".md".length());
    }

    // helper to extract the document title from a markdown file
    private String extractTitle(String content, String fallbackTitle) {
        return content.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).trim())
                .filter(title -> !title.isBlank())
                .findFirst()
                .orElse(fallbackTitle);
    }

    // helper to create an empty file name for documents with no file name
    private String getSortableFilename(Resource resource) {
        String filename = resource.getFilename();

        return filename == null ? "" : filename;
    }
}