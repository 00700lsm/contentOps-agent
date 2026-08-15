package com.contentopsagent.evaluation.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.evaluation.dataset.EvaluationDatasetLoader;
import com.contentopsagent.evaluation.result.EvaluationResult;
import com.contentopsagent.document.DocumentIngestionService;
import com.contentopsagent.rag.RagService;
import com.contentopsagent.rag.model.AnswerSource;
import com.contentopsagent.rag.model.RagAnswer;
import com.contentopsagent.retrieval.RetrievalService;
import com.contentopsagent.retrieval.model.RetrievalResult;
import com.contentopsagent.retrieval.model.RetrievedChunk;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvaluationRunnerTest {

    @TempDir
    Path tempDir;

    @Mock
    private RetrievalService retrievalService;

    @Mock
    private RagService ragService;

    @Mock
    private DocumentIngestionService ingestionService;

    @Test
    void writesMetricsAndPerQueryRanking() throws Exception {
        Path dataset = tempDir.resolve("retrieval.jsonl");
        Path results = tempDir.resolve("results");
        Files.writeString(dataset, """
                {"id":"retrieval-001","category":"SEMANTIC","question":"15세 콘텐츠 공개 기준은?","expectedDocuments":["age-rating-policy.md"],"answerable":true}
                {"id":"retrieval-011","category":"NO_ANSWER","question":"해외 판권 계약 담당자가 누구야?","expectedDocuments":[],"answerable":false}
                """);

        when(retrievalService.search(anyString())).thenReturn(new RetrievalResult(
                "q",
                5,
                List.of(
                        new RetrievedChunk(1, "검수", "publishing-guide.md", "1. 공개 전 체크리스트", 0, 0.8),
                        new RetrievedChunk(2, "청소년", "youth-protection-policy.md", "3. 15세 콘텐츠와 보호 설정", 1, 0.75),
                        new RetrievedChunk(3, "15세", "age-rating-policy.md", "3.2 공개 조건", 2, 0.7)
                ),
                1,
                2
        ));

        when(ragService.generate(anyString(), any())).thenReturn(new RagAnswer(
                "15세 콘텐츠는 연령 등급 검수 후 공개할 수 있습니다.",
                List.of(new AnswerSource("age-rating-policy.md", "3.2 공개 조건")),
                5,
                null,
                null
        ));

        AppProperties properties = new AppProperties();
        properties.getEvaluate().setDatasetPath(dataset.toString());
        properties.getEvaluate().setResultsDir(results.toString());

        EvaluationRunner runner = new EvaluationRunner(
                properties,
                new EvaluationDatasetLoader(new ObjectMapper()),
                retrievalService,
                ragService,
                ingestionService,
                new ObjectMapper()
        );

        EvaluationResult result = runner.run();

        assertThat(result.retrievalStrategy()).isEqualTo("Vector Search Only");
        assertThat(result.metrics().queryCount()).isEqualTo(2);
        assertThat(result.metrics().scoredQueryCount()).isEqualTo(1);
        assertThat(result.metrics().hitRateAtK()).isEqualTo(1.0);
        assertThat(result.queries().getFirst().expectedDocumentRank()).isEqualTo(3);
        assertThat(result.queries().getFirst().actualTopK()).extracting(hit -> hit.documentName())
                .containsExactly("publishing-guide.md", "youth-protection-policy.md", "age-rating-policy.md");
        assertThat(result.queries().getFirst().answer()).isNotBlank();
        assertThat(result.queries().getFirst().primaryFailureType()).isEqualTo("RANKING");
        assertThat(Files.list(results).toList()).isNotEmpty();
    }
}
