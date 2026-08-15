package com.contentopsagent.evaluation.dataset;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EvaluationQuery(
        String id,
        String category,
        String question,
        List<String> expectedDocuments,
        List<String> expectedTools,
        List<Long> expectedContentIds,
        boolean answerable
) {
    public List<String> expectedDocuments() {
        return expectedDocuments == null ? List.of() : expectedDocuments;
    }

    public List<String> expectedTools() {
        return expectedTools == null ? List.of() : expectedTools;
    }

    public List<Long> expectedContentIds() {
        return expectedContentIds == null ? List.of() : expectedContentIds;
    }
}
