package com.contentopsagent.retrieval;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.document.DocumentIngestionService;
import com.contentopsagent.retrieval.model.RetrievalResult;
import com.contentopsagent.support.TestAiConfig;
import org.junit.jupiter.api.BeforeEach;
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
class PgVectorSearchIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("contentops")
            .withUsername("contentops")
            .withPassword("contentops");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("app.documents.path", () -> "data/documents");
        registry.add("app.rag.top-k", () -> "5");
    }

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private RetrievalService retrievalService;

    @BeforeEach
    void ingest() {
        ingestionService.resetAndIngest();
    }

    @Test
    void storesChunksAndReturnsTopKWithMetadata() {
        RetrievalResult result = retrievalService.search("M-03 오류의 의미가 뭐야?");

        assertThat(result.chunks()).isNotEmpty();
        assertThat(result.chunks().size()).isLessThanOrEqualTo(5);
        assertThat(result.chunks().getFirst().rank()).isEqualTo(1);
        assertThat(result.chunks()).allSatisfy(chunk -> {
            assertThat(chunk.documentName()).isNotBlank();
            assertThat(chunk.section()).isNotBlank();
            assertThat(chunk.content()).isNotBlank();
            assertThat(chunk.similarityScore()).isNotNull();
        });
        assertThat(result.chunks().stream().map(chunk -> chunk.documentName()).toList())
                .contains("metadata-guide.md");
    }
}
