package com.contentopsagent.evaluation.result;

public record AgentEvaluationSummary(
        int queryCount,
        double toolSelectionAccuracy,
        double sequenceAccuracy,
        int matchCount,
        int wrongOrderCount,
        int missingToolCount,
        int unnecessaryToolCount,
        int wrongToolCount,
        double avgLatencyMs
) {
}
