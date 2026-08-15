package com.contentopsagent.evaluation.result;

import java.util.List;

public record EvaluationSummary(
        int queryCount,
        int scoredQueryCount,
        double hitRateAtK,
        double recallAtK,
        double mrr,
        double avgEmbeddingLatencyMs,
        double avgRetrievalLatencyMs,
        double avgLlmLatencyMs,
        double avgEndToEndLatencyMs,
        Long totalPromptTokens,
        Long totalCompletionTokens
) {
    public static EvaluationSummary from(List<QueryEvaluationResult> results) {
        List<QueryEvaluationResult> scored = results.stream()
                .filter(QueryEvaluationResult::answerable)
                .filter(result -> result.expectedDocuments() != null && !result.expectedDocuments().isEmpty())
                .toList();
        if (results.isEmpty()) {
            return new EvaluationSummary(0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, null);
        }
        double hitRate = scored.isEmpty()
                ? 0.0
                : scored.stream().filter(QueryEvaluationResult::hit).count() / (double) scored.size();
        double recall = scored.stream().mapToDouble(QueryEvaluationResult::recall).average().orElse(0.0);
        double mrr = scored.stream().mapToDouble(QueryEvaluationResult::reciprocalRank).average().orElse(0.0);
        boolean hasPrompt = results.stream().anyMatch(result -> result.promptTokens() != null);
        boolean hasCompletion = results.stream().anyMatch(result -> result.completionTokens() != null);
        return new EvaluationSummary(
                results.size(),
                scored.size(),
                hitRate,
                recall,
                mrr,
                results.stream().mapToLong(QueryEvaluationResult::embeddingLatencyMs).average().orElse(0.0),
                results.stream().mapToLong(QueryEvaluationResult::retrievalLatencyMs).average().orElse(0.0),
                results.stream().mapToLong(QueryEvaluationResult::llmLatencyMs).average().orElse(0.0),
                results.stream().mapToLong(QueryEvaluationResult::endToEndLatencyMs).average().orElse(0.0),
                hasPrompt ? results.stream().mapToLong(result -> result.promptTokens() == null ? 0L : result.promptTokens()).sum() : null,
                hasCompletion ? results.stream().mapToLong(result -> result.completionTokens() == null ? 0L : result.completionTokens()).sum() : null
        );
    }
}
