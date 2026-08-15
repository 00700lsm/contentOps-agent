package com.contentopsagent.evaluation.result;

import java.util.List;

public record EvaluationSummary(
        int queryCount,
        int scoredQueryCount,
        double hitRateAtK,
        double recallAtK,
        double mrr
) {
    public static EvaluationSummary from(List<QueryEvaluationResult> results) {
        List<QueryEvaluationResult> scored = results.stream()
                .filter(QueryEvaluationResult::answerable)
                .filter(result -> result.expectedDocuments() != null && !result.expectedDocuments().isEmpty())
                .toList();
        if (scored.isEmpty()) {
            return new EvaluationSummary(results.size(), 0, 0.0, 0.0, 0.0);
        }
        double hitRate = scored.stream().filter(QueryEvaluationResult::hit).count() / (double) scored.size();
        double recall = scored.stream().mapToDouble(QueryEvaluationResult::recall).average().orElse(0.0);
        double mrr = scored.stream().mapToDouble(QueryEvaluationResult::reciprocalRank).average().orElse(0.0);
        return new EvaluationSummary(results.size(), scored.size(), hitRate, recall, mrr);
    }
}
