package com.contentopsagent.evaluation.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import org.junit.jupiter.api.Test;

class EvaluationSummaryTest {

    @Test
    void aggregatesAnswerableQueriesOnly() {
        QueryEvaluationResult hit = query("retrieval-001", true, List.of("age-rating-policy.md"), true, 1.0, 1.0);
        QueryEvaluationResult miss = query("retrieval-002", true, List.of("metadata-guide.md"), false, 0.0, 0.0);
        QueryEvaluationResult noAnswer = query("retrieval-011", false, List.of(), false, 0.0, 0.0);

        EvaluationSummary summary = EvaluationSummary.from(List.of(hit, miss, noAnswer));

        assertThat(summary.queryCount()).isEqualTo(3);
        assertThat(summary.scoredQueryCount()).isEqualTo(2);
        assertThat(summary.hitRateAtK()).isEqualTo(0.5);
        assertThat(summary.recallAtK()).isEqualTo(0.5);
        assertThat(summary.mrr()).isCloseTo(0.5, within(0.0001));
    }

    private QueryEvaluationResult query(
            String id,
            boolean answerable,
            List<String> expected,
            boolean hit,
            double recall,
            double rr
    ) {
        return new QueryEvaluationResult(
                id,
                answerable ? "SEMANTIC" : "NO_ANSWER",
                "question",
                expected,
                answerable,
                List.of(),
                hit ? 1 : null,
                hit,
                recall,
                rr,
                "답변",
                List.of(),
                1,
                10,
                20,
                30,
                null,
                null,
                hit ? "NEEDS_GENERATION_REVIEW" : "RETRIEVAL"
        );
    }
}
