package com.contentopsagent.evaluation.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.evaluation.dataset.EvaluationDatasetLoader;
import com.contentopsagent.evaluation.result.EvaluationResult;
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
                        new RetrievedChunk(2, "15세", "age-rating-policy.md", "3.2 공개 조건", 1, 0.7)
                ),
                1,
                2
        ));

        AppProperties properties = new AppProperties();
        properties.getEvaluate().setDatasetPath(dataset.toString());
        properties.getEvaluate().setResultsDir(results.toString());

        EvaluationRunner runner = new EvaluationRunner(
                properties,
                new EvaluationDatasetLoader(new ObjectMapper()),
                retrievalService,
                new ObjectMapper()
        );

        EvaluationResult result = runner.run();

        assertThat(result.retrievalStrategy()).isEqualTo("Vector Search Only");
        assertThat(result.metrics().queryCount()).isEqualTo(2);
        assertThat(result.metrics().scoredQueryCount()).isEqualTo(1);
        assertThat(result.metrics().hitRateAtK()).isEqualTo(1.0);
        assertThat(result.queries().getFirst().expectedDocumentRank()).isEqualTo(2);
        assertThat(result.queries().getFirst().actualTopK()).extracting(hit -> hit.documentName())
                .containsExactly("publishing-guide.md", "age-rating-policy.md");
        assertThat(Files.list(results).toList()).isNotEmpty();
    }
}
