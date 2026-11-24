package com.dando.pacepilot.knowledge.loader;

import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import com.dando.pacepilot.knowledge.exception.TrainingDocumentLoadingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ClasspathPdfDocumentLoader implements TrainingDocumentLoader {

    private static final String RESOURCE_PATTERN = "classpath*:knowledge/*.pdf";

    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    @Override
    public List<TrainingDocument> loadDocuments() {
        Resource[] resources;

        try {
            resources = resourceResolver.getResources(RESOURCE_PATTERN);
        } catch (IOException exception) {
            throw new TrainingDocumentLoadingException("Unable to locate PDF training documents", exception);
        }

        if (resources.length == 0) {
            return List.of();
        }

        Arrays.sort(resources, Comparator.comparing(this::getSortableFilename));

        List<TrainingDocument> documents = new ArrayList<>();

        for (Resource resource : resources) {
            documents.add(loadDocument(resource));
        }

        return List.copyOf(documents);
    }

    // helper to extract document metadata from a resource object
    private TrainingDocument loadDocument(Resource resource) {
        String filename = resource.getFilename();

        if (filename == null || filename.isBlank()) {
            throw new TrainingDocumentLoadingException("A PDF training resource has no filename: " + resource.getDescription());
        }

        try (InputStream input = resource.getInputStream();
             PDDocument pdf = Loader.loadPDF(input.readAllBytes())) {

            if (!pdf.getCurrentAccessPermission().canExtractContent()) {
                throw new IOException("Text extraction is not permitted for this PDF.");
            }

            String fallbackTitle = filename.length() > ".pdf".length()
                    ? filename.substring(0, filename.length() - ".pdf".length()) : filename;

            String title = pdf.getDocumentInformation().getTitle();

            title = title == null || title.isBlank() ? fallbackTitle : title.strip();

            String content = extractContent(pdf);

            return new TrainingDocument(
                    "pdf:" + filename,
                    title,
                    "knowledge/" + filename,
                    content
            );

        } catch (IOException exception) {
            throw new TrainingDocumentLoadingException(
                    "Unable to load PDF training document '" + filename + "': " + exception.getMessage(), exception);
        }
    }

    private String extractContent(PDDocument pdf) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);

        StringBuilder content = new StringBuilder();

        // Create a markdown-like section title for each page in the document. The page number is the section title
        for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
            stripper.setStartPage(page);
            stripper.setEndPage(page);

            String pageText = stripper.getText(pdf).strip();

            if (pageText.isBlank()) {
                continue;
            }

            content.append("## Page ").append(page).append("\n\n");

            content.append(escapeHeadingLines(pageText)).append("\n\n");
        }

        if (content.isEmpty()) {
            throw new IOException("No extractable text was found. Scanned PDFs require OCR.");
        }

        return content.toString();
    }

    // Helper to prevent mardkown heading-like text extracted from a PDF from accidentally creating new chunks
    private String escapeHeadingLines(String text) {
        return text.lines()
                .map(line -> {String trimmed = line.stripLeading();
                    // add backslash (\) before any # characters to prevent new chunks
                    return trimmed.startsWith("# ") || trimmed.startsWith("## ") ? "\\" + line : line;
                })
                .collect(Collectors.joining("\n"));
    }

    // Helper to create an empty file name for documents with no file name
    private String getSortableFilename(Resource resource) {
        String filename = resource.getFilename();

        return filename == null ? "" : filename;
    }
}