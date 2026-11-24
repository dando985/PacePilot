package com.dando.pacepilot.knowledge.chunking;

import com.dando.pacepilot.knowledge.domain.TrainingChunk;
import com.dando.pacepilot.knowledge.domain.TrainingDocument;
import org.springframework.stereotype.Component;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MarkdownSectionChunker {

    private static final String DOCUMENT_TITLE_PREFIX = "# ";
    private static final String SECTION_TITLE_PREFIX = "## ";

    // defines max number of characters in content that each chunk can contain
    private static final int MAX_CHUNK_CHARACTERS = 2000;

    // defines how many characters neighboring chunks share, adjusted toward a word boundary.
    private static final int CHUNK_OVERLAP = 200;

    // reusable regular expression for finding blank lines
    private static final Pattern PARAGRAPH_BREAK = Pattern.compile("\\n\\h*\\n");

    public List<TrainingChunk> createChunks(TrainingDocument document) {
        Objects.requireNonNull(document, "Training document cannot be null");

        List<TrainingChunk> chunks = new ArrayList<>();

        // Every document begins with an artificial introduction
        String currentSectionTitle = "Introduction";
        StringBuilder currentContent = new StringBuilder();
        int chunkNumber = 1;

        String[] lines = document.content().split("\\R", -1);

        for (String line : lines) {
            String trimmedLine = line.trim();

            // Check if current line starts with a new section header
            if (trimmedLine.startsWith(SECTION_TITLE_PREFIX)) {

                // add previous section's accumulated content to chunks array and return the next available chunk number
                chunkNumber = addChunksIfNotBlank(chunks, document, currentSectionTitle, currentContent, chunkNumber);

                // empty the buffer to reuse for new section
                currentContent.setLength(0);

                // remove heading prefix (##) to obtain new section's name
                currentSectionTitle = trimmedLine.substring(SECTION_TITLE_PREFIX.length()).trim();

                continue;
            }

            // skips lines starting with the document title ("# ")
            if (trimmedLine.startsWith(DOCUMENT_TITLE_PREFIX)) {
                continue;
            }

            // add content to string builder
            currentContent.append(line).append('\n');
        }

        // add last section's accumulated content to chunks array
        addChunksIfNotBlank(chunks, document, currentSectionTitle, currentContent, chunkNumber);

        return List.copyOf(chunks);
    }

    // helper to split one section in a document and return the next available chunk number
    private int addChunksIfNotBlank(List<TrainingChunk> chunks, TrainingDocument document, String sectionTitle,
                                    StringBuilder sectionContent, int chunkNumber) {

        String content = sectionContent.toString().strip();

        if (content.isBlank()) {
            return chunkNumber;
        }

        // utility to identify likely sentence boundaries
        BreakIterator sentences = BreakIterator.getSentenceInstance(Locale.ROOT);
        sentences.setText(content);

        // starting index of the current chunk
        int start = 0;

        while (start < content.length()) {
            // determine where the current chunk should end, considering the length limit
            // and natural text boundaries.
            int end = findChunkEnd(content, start, sentences);

            // extract the text between start and determined end
            String part = content.substring(start, end).strip();

            // adds chunk to chunks array
            if (!part.isBlank()) {
                chunks.add(new TrainingChunk(
                        document.id() + "-chunk-" + chunkNumber,
                        document.id(),
                        document.title(),
                        document.source(),
                        sectionTitle,
                        chunkNumber,
                        part
                ));

                chunkNumber++;
            }

            if (end == content.length()) {
                break;
            }

            start = findNextChunkStart(content, start, end);
        }

        return chunkNumber;
    }

    // private helper to determine ending position of a chunk within the size limit
    private int findChunkEnd(String content, int start, BreakIterator sentences) {
        int maximumEnd = start + Math.min(MAX_CHUNK_CHARACTERS, content.length() - start);

        if (maximumEnd == content.length()) {
            return maximumEnd;
        }

        // Moves the boundary back if necessary to avoid splitting a UTF-16 surrogate pair,
        // such as one used to represent an emoji.
        maximumEnd = safeCharacterBoundary(content, maximumEnd);

        // Prefer natural boundaries in the latter half of the chunk to avoid producing very small chunks.
        int minimumEnd = start + MAX_CHUNK_CHARACTERS / 2;

        // Creates a matcher that searches the content for blank-line paragraph separators.
        Matcher paragraphs = PARAGRAPH_BREAK.matcher(content);

        // Restricts that search to the range between the preferred minimum and maximum ending positions.
        paragraphs.region(minimumEnd, maximumEnd);

        // initialize paragraph separator value.
        // -1 means no paragraph separator has been found
        int paragraphEnd = -1;

        // find the final value that points just after the last matching separator
        while (paragraphs.find()) {
            paragraphEnd = paragraphs.end();
        }

        if (paragraphEnd != -1) {
            return paragraphEnd;
        }

        // find sentence boundary if no paragraph boundary exists
        int sentenceEnd = sentences.isBoundary(maximumEnd) ? maximumEnd : sentences.preceding(maximumEnd);
        if (sentenceEnd >= minimumEnd) {
            return sentenceEnd;
        }

        // search backward from maximum ending position for potential ending position
        for (int end = maximumEnd; end >= minimumEnd; end--) {
            if (Character.isWhitespace(content.charAt(end - 1))) {
                return end;
            }
        }

        // Uninterrupted text still needs a hard limit.
        return maximumEnd;
    }

    // helper to choose overlap and next starting position
    private int findNextChunkStart(String content, int start, int end) {

        // starts the next chunk CHUNK_OVERLAP units before the current chunk ends
        // e.g.,    Current chunk: start = 0, end = 2000
        //          Next starting position: 1800
        int nextStart = Math.max(start + 1, end - CHUNK_OVERLAP);

        // adjusts the candidate if it falls between the two halves of a surrogate pair
        nextStart = safeCharacterBoundary(content, nextStart);

        // if the character immediately before the proposed start is whitespace,
        // the position is already suitable for beginning at a word boundary
        if (Character.isWhitespace(content.charAt(nextStart - 1))) {
            return nextStart;
        }

        // begin on a word boundary when one is near the overlap target.
        int searchEnd = Math.min(end, nextStart + CHUNK_OVERLAP / 2);

        // examines each position in that search range until whitespace is found
        for (int index = nextStart; index < searchEnd; index++) {
            if (Character.isWhitespace(content.charAt(index))) {
                return index + 1;
            }
        }

        return nextStart;
    }

    // helper to protect characters represented by surrogate pairs.
    private int safeCharacterBoundary(String text, int index) {
        // checks both sides of the boundary position for surrogate pairs
        if (index > 0 && index < text.length() && Character.isHighSurrogate(text.charAt(index - 1))
                && Character.isLowSurrogate(text.charAt(index))) {
            // move boundary position one step back
            return index - 1;
        }

        return index;
    }
}