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
        assertThat(queries.getLast().expectedTools()).isEmpty();
    }

    @Test
    void loadsExpectedToolsFromAgentDataset() throws Exception {
        Path dataset = tempDir.resolve("agent-tools.jsonl");
        Files.writeString(dataset, """
                {"id":"tool-002","category":"CONTENT_SEARCH","question":"이번 달 공개 예정 액션 콘텐츠 알려줘.","expectedTools":["search_contents"],"expectedContentIds":[100],"answerable":true}
                """);

        List<EvaluationQuery> queries = new EvaluationDatasetLoader(new ObjectMapper()).load(dataset);

        assertThat(queries).hasSize(1);
        assertThat(queries.getFirst().expectedTools()).containsExactly("search_contents");
        assertThat(queries.getFirst().expectedContentIds()).containsExactly(100L);
    }

    @Test
    void loadsCheckedInAgentToolDataset() {
        List<EvaluationQuery> queries = new EvaluationDatasetLoader(new ObjectMapper())
                .load(java.nio.file.Path.of("evaluation/datasets/agent-tools.jsonl"));

        assertThat(queries).extracting(EvaluationQuery::id)
                .contains("tool-001", "tool-002", "tool-003");
        assertThat(queries)
                .filteredOn(query -> "POLICY_ONLY".equals(query.category()))
                .allSatisfy(query -> assertThat(query.expectedTools()).containsExactly("search_policy_documents"));
        assertThat(queries)
                .filteredOn(query -> "CONTENT_SEARCH".equals(query.category()))
                .allSatisfy(query -> assertThat(query.expectedTools()).containsExactly("search_contents"));
        assertThat(queries)
                .filteredOn(query -> "CONTENT_DETAIL".equals(query.category()))
                .allSatisfy(query -> assertThat(query.expectedTools()).containsExactly("get_content_detail"));
        assertThat(queries)
                .filteredOn(query -> "MULTI_TOOL".equals(query.category()))
                .allSatisfy(query -> assertThat(query.expectedTools())
                        .containsExactly("get_content_detail", "search_policy_documents"));
        assertThat(queries).extracting(EvaluationQuery::id).contains("tool-007", "tool-008");
    }
}
