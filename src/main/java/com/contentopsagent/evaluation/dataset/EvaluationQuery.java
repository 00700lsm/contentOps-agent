package com.contentopsagent.evaluation.dataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvaluationQuery(
        String id,
        String category,
        String question,
        List<String> expectedDocuments,
        boolean answerable
) {
    public List<String> expectedDocuments() {
        return expectedDocuments == null ? List.of() : expectedDocuments;
    }
}
