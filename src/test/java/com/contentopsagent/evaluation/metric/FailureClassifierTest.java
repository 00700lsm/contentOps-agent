package com.contentopsagent.evaluation.metric;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FailureClassifierTest {

    @Test
    void classifiesRetrievalWhenExpectedDocumentIsMissing() {
        assertThat(FailureClassifier.classify(true, false, null, "아무 답")).isEqualTo("RETRIEVAL");
    }

    @Test
    void classifiesRankingWhenExpectedDocumentIsLow() {
        assertThat(FailureClassifier.classify(true, true, 3, "아무 답")).isEqualTo("RANKING");
        assertThat(FailureClassifier.classify(true, true, 1, "아무 답")).isEqualTo("NEEDS_GENERATION_REVIEW");
    }

    @Test
    void classifiesNoAnswerWhenModelAnswersWithoutEvidence() {
        assertThat(FailureClassifier.classify(false, false, null, "홍길동이 담당자입니다.")).isEqualTo("NO_ANSWER");
        assertThat(FailureClassifier.classify(false, false, null, "현재 제공된 문서에서는 확인할 수 없습니다.")).isEqualTo("NONE");
    }
}
