package com.contentopsagent.rag;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.retrieval.model.RetrievedChunk;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContextBuilderTest {

    private final ContextBuilder contextBuilder = new ContextBuilder();

    @Test
    void includesDocumentAndSectionForEachChunk() {
        String context = contextBuilder.build(List.of(
                new RetrievedChunk(1, "검수 완료 후 공개한다.", "age-rating-policy.md", "3.2 공개 조건", 0, 0.9),
                new RetrievedChunk(2, "청소년 프로필 제한", "youth-protection-policy.md", "2. 프로필 제한", 1, 0.8)
        ));

        assertThat(context).contains("[Source 1]");
        assertThat(context).contains("Document: age-rating-policy.md");
        assertThat(context).contains("Section: 3.2 공개 조건");
        assertThat(context).contains("검수 완료 후 공개한다.");
        assertThat(context).contains("[Source 2]");
        assertThat(context).contains("youth-protection-policy.md");
    }

    @Test
    void returnsEmptyStringWhenNoChunks() {
        assertThat(contextBuilder.build(List.of())).isEmpty();
    }
}
