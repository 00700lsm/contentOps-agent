package com.contentopsagent.evaluation.result;

import java.util.List;

public record QueryEvaluationResult(
        String id,
        String category,
        String question,
        List<String> expectedDocuments,
        boolean answerable,
        List<ActualHit> actualTopK,
        Integer expectedDocumentRank,
        boolean hit,
        double recall,
        double reciprocalRank,
        long retrievalLatencyMs
) {
}
