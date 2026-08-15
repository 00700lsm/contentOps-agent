package com.contentopsagent.tool;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.content.model.ContentRecord;
import com.contentopsagent.support.TestAiConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
class ContentToolsIT {

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
        registry.add("app.contents.path", () -> "data/contents/contents.json");
    }

    @Autowired
    private ContentSearchTool contentSearchTool;

    @Autowired
    private ContentDetailTool contentDetailTool;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void searchContentsFiltersScheduledActionInAugust2026() throws Exception {
        String json = contentSearchTool.searchContents("액션", null, "SCHEDULED", null, null, "2026-08", null, null);
        List<ContentRecord> found = objectMapper.readValue(json, new TypeReference<>() {
        });

        assertThat(found).extracting(ContentRecord::id).containsExactly(100L);
        assertThat(found.getFirst().title()).isEqualTo("나이트 레이드");
    }

    @Test
    void getContentDetailReturnsStatusForId100() throws Exception {
        String json = contentDetailTool.getContentDetail(100);
        ContentRecord record = objectMapper.readValue(json, ContentRecord.class);

        assertThat(record.id()).isEqualTo(100);
        assertThat(record.status()).isEqualTo("SCHEDULED");
        assertThat(record.genre()).isEqualTo("액션");
    }

    @Test
    void getContentDetailReturnsNotFoundMessage() {
        assertThat(contentDetailTool.getContentDetail(999)).contains("콘텐츠를 찾지 못했습니다");
    }
}
