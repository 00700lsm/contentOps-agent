package com.contentopsagent.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.contentopsagent.rag.ContextBuilder;
import com.contentopsagent.retrieval.RetrievalService;
import com.contentopsagent.retrieval.model.RetrievalResult;
import com.contentopsagent.retrieval.model.RetrievedChunk;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicySearchToolTest {

    @Mock
    private RetrievalService retrievalService;

    @Test
    void searchesPolicyDocumentsAndReturnsContext() {
        when(retrievalService.search("OPS-101은 무엇인가?")).thenReturn(new RetrievalResult(
                "OPS-101은 무엇인가?",
                5,
                List.of(new RetrievedChunk(1, "OPS-101은 공개 파이프라인 실패 코드다.", "operations-faq.md", "2. OPS-101", 0, 0.9)),
                10L,
                20L
        ));

        String result = new PolicySearchTool(retrievalService, new ContextBuilder())
                .searchPolicyDocuments("OPS-101은 무엇인가?");

        verify(retrievalService).search("OPS-101은 무엇인가?");
        assertThat(result).contains("operations-faq.md");
        assertThat(result).contains("2. OPS-101");
        assertThat(result).contains("공개 파이프라인 실패 코드");
    }

    @Test
    void returnsNoDocumentMessageWhenEmpty() {
        when(retrievalService.search("없는 정책")).thenReturn(new RetrievalResult("없는 정책", 5, List.of(), 1L, 1L));

        String result = new PolicySearchTool(retrievalService, new ContextBuilder())
                .searchPolicyDocuments("없는 정책");

        assertThat(result).isEqualTo("관련 정책 문서를 찾지 못했습니다.");
    }
}
