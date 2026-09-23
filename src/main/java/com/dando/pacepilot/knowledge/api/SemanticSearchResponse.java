package com.dando.pacepilot.knowledge.api;

import java.util.List;

public record SemanticSearchResponse(
        String query,
        int resultCount,
        List<SemanticSearchResultResponse> results
) {
}