package com.contentopsagent.evaluation.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import org.junit.jupiter.api.Test;

class RetrievalMetricsTest {

    @Test
    void hitWhenExpectedDocumentIsInTopK() {
        List<String> expected = List.of("age-rating-policy.md");
        List<String> actual = List.of("publishing-guide.md", "youth-protection-policy.md", "age-rating-policy.md");

        assertThat(RetrievalMetrics.hit(expected, actual)).isTrue();
        assertThat(RetrievalMetrics.recall(expected, actual)).isEqualTo(1.0);
        assertThat(RetrievalMetrics.firstExpectedRank(expected, actual)).isEqualTo(3);
        assertThat(RetrievalMetrics.reciprocalRank(expected, actual)).isCloseTo(1.0 / 3.0, within(0.0001));
    }

    @Test
    void missWhenExpectedDocumentIsAbsent() {
        List<String> expected = List.of("metadata-guide.md");
        List<String> actual = List.of("operations-faq.md", "publishing-guide.md");

        assertThat(RetrievalMetrics.hit(expected, actual)).isFalse();
        assertThat(RetrievalMetrics.recall(expected, actual)).isEqualTo(0.0);
        assertThat(RetrievalMetrics.firstExpectedRank(expected, actual)).isEqualTo(-1);
        assertThat(RetrievalMetrics.reciprocalRank(expected, actual)).isEqualTo(0.0);
    }

    @Test
    void recallSupportsMultipleExpectedDocuments() {
        List<String> expected = List.of("youth-protection-policy.md", "age-rating-policy.md");
        List<String> actual = List.of("youth-protection-policy.md", "publishing-guide.md");

        assertThat(RetrievalMetrics.recall(expected, actual)).isEqualTo(0.5);
        assertThat(RetrievalMetrics.hit(expected, actual)).isTrue();
        assertThat(RetrievalMetrics.reciprocalRank(expected, actual)).isEqualTo(1.0);
    }
}
