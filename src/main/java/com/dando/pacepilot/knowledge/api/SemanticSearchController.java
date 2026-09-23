package com.dando.pacepilot.knowledge.api;

import com.dando.pacepilot.retrieval.SemanticRetriever;
import com.dando.pacepilot.retrieval.SemanticSearchResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class SemanticSearchController {

    private final SemanticRetriever semanticRetriever;

    public SemanticSearchController(SemanticRetriever semanticRetriever) {
        this.semanticRetriever = semanticRetriever;
    }

    @PostMapping("/search")
    public SemanticSearchResponse search(@Valid @RequestBody SearchTrainingKnowledgeRequest request) {
        int limit = request.resolvedLimit();

        List<SemanticSearchResult> results = semanticRetriever.search(request.query(), limit);

        List<SemanticSearchResultResponse> responseResults =
                results.stream()
                        .map(SemanticSearchResultResponse::from)
                        .toList();

        return new SemanticSearchResponse(
                request.query(),
                responseResults.size(),
                responseResults
        );
    }
}