package com.contentopsagent.evaluation.result;

import java.util.List;

public record AgentQueryEvaluationResult(
        String id,
        String category,
        String question,
        List<String> expectedTools,
        List<String> actualTools,
        List<String> missingTools,
        List<String> extraTools,
        boolean setMatch,
        boolean sequenceMatch,
        String failureType,
        String answer,
        long latencyMs
) {
}
