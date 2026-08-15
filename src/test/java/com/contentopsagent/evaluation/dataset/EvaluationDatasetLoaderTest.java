package com.contentopsagent.evaluation.dataset;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EvaluationDatasetLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsJsonlQueries() throws Exception {
        Path dataset = tempDir.resolve("retrieval.jsonl");
        Files.writeString(dataset, """
                {"id":"retrieval-001","category":"SEMANTIC","question":"15세 콘텐츠 공개 기준은?","expectedDocuments":["age-rating-policy.md"],"answerable":true}
                {"id":"retrieval-011","category":"NO_ANSWER","question":"해외 판권 계약 담당자가 누구야?","expectedDocuments":[],"answerable":false}
                """);

        List<EvaluationQuery> queries = new EvaluationDatasetLoader(new ObjectMapper()).load(dataset);

        assertThat(queries).hasSize(2);
        assertThat(queries.getFirst().id()).isEqualTo("retrieval-001");
        assertThat(queries.getFirst().expectedDocuments()).containsExactly("age-rating-policy.md");
        assertThat(queries.getLast().answerable()).isFalse();
        assertThat(queries.getLast().expectedDocuments()).isEmpty();
    }
}
