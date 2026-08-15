package com.contentopsagent.evaluation.runner;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.document.DocumentIngestionService;
import com.contentopsagent.evaluation.result.EvaluationResult;
import com.contentopsagent.support.TestAiConfig;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@Import(TestAiConfig.class)
class EvaluationRunnerIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("contentops")
            .withUsername("contentops")
            .withPassword("contentops");

    static final Path RESULTS_DIR = Path.of("build/tmp/evaluation-it");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.documents.path", () -> "data/documents");
        registry.add("app.evaluate.dataset-path", () -> "evaluation/datasets/retrieval.jsonl");
        registry.add("app.evaluate.results-dir", () -> RESULTS_DIR.toString());
    }

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private EvaluationRunner evaluationRunner;

    @Test
    void runsRetrievalEvaluationAgainstSampleDataset() {
        ingestionService.resetAndIngest();

        EvaluationResult result = evaluationRunner.run();

        assertThat(result.queries()).hasSize(12);
        assertThat(result.metrics().queryCount()).isEqualTo(12);
        assertThat(result.metrics().scoredQueryCount()).isEqualTo(10);
        assertThat(result.retrievalStrategy()).isEqualTo("Vector Search Only");
        assertThat(result.queries()).allSatisfy(query -> {
            assertThat(query.actualTopK()).isNotNull();
            assertThat(query.retrievalLatencyMs()).isGreaterThanOrEqualTo(0);
            assertThat(query.answer()).isNotBlank();
            assertThat(query.primaryFailureType()).isNotBlank();
        });
        assertThat(result.queries())
                .filteredOn(query -> "NO_ANSWER".equals(query.category()))
                .hasSize(2);
    }
}
