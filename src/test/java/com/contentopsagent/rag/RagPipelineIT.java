package com.contentopsagent.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contentopsagent.document.DocumentIngestionService;
import com.contentopsagent.question.dto.QuestionResponse;
import com.contentopsagent.question.service.QuestionService;
import com.contentopsagent.support.TestAiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestAiConfig.class)
class RagPipelineIT {

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
    }

    @Autowired
    private DocumentIngestionService ingestionService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void ingest() {
        ingestionService.resetAndIngest();
    }

    @Test
    void generatesAnswerFromRetrievedContextAndReturnsSources() {
        QuestionResponse response = questionService.ask("15세 콘텐츠의 공개 조건이 뭐야?");

        assertThat(response.answer()).isEqualTo("테스트 답변입니다.");
        assertThat(response.sources()).isNotEmpty();
        assertThat(response.sources()).allSatisfy(source -> {
            assertThat(source.document()).endsWith(".md");
            assertThat(source.section()).isNotBlank();
        });
    }

    @Test
    void questionApiDoesNotExposeSimilarityScore() throws Exception {
        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"CONTENT_BLOCKED는 언제 사용하는가?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("테스트 답변입니다."))
                .andExpect(jsonPath("$.sources").isArray())
                .andExpect(jsonPath("$.sources[0].document").exists())
                .andExpect(jsonPath("$.sources[0].section").exists())
                .andExpect(jsonPath("$.sources[0].similarityScore").doesNotExist());
    }
}
