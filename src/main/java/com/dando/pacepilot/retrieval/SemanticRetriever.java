package com.dando.pacepilot.retrieval;

import java.util.List;

public interface SemanticRetriever {

    List<SemanticSearchResult> search(String query, int limit);
}