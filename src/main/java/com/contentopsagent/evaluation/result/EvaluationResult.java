package com.contentopsagent.evaluation.result;

import java.time.Instant;
import java.util.List;

public record EvaluationResult(
        Instant executedAt,
        String documentDataset,
        String evaluationDataset,
        String embeddingModel,
        String chatModel,
        int chunkSize,
        int chunkOverlap,
        int topK,
        String retrievalStrategy,
        EvaluationSummary metrics,
        List<QueryEvaluationResult> queries
) {
}
