package com.contentopsagent.evaluation.result;

import java.time.Instant;
import java.util.List;

public record AgentEvaluationResult(
        Instant executedAt,
        String evaluationDataset,
        String chatModel,
        String workflow,
        AgentEvaluationSummary metrics,
        List<AgentQueryEvaluationResult> queries
) {
}
