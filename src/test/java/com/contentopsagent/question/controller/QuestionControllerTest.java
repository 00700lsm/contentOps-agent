package com.contentopsagent.question.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.contentopsagent.question.dto.QuestionResponse;
import com.contentopsagent.question.dto.SourceDto;
import com.contentopsagent.question.service.QuestionService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @Test
    void rejectsBlankQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_QUESTION"));

        verifyNoInteractions(questionService);
    }

    @Test
    void rejectsMissingQuestion() throws Exception {
        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_QUESTION"));
    }

    @Test
    void returnsAnswerAndSources() throws Exception {
        when(questionService.ask("15세 콘텐츠의 공개 조건이 뭐야?"))
                .thenReturn(new QuestionResponse(
                        "연령 등급 검수 완료 후 공개할 수 있습니다.",
                        List.of(new SourceDto("age-rating-policy.md", "3.2 공개 조건"))
                ));

        mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"15세 콘텐츠의 공개 조건이 뭐야?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("연령 등급 검수 완료 후 공개할 수 있습니다."))
                .andExpect(jsonPath("$.sources[0].document").value("age-rating-policy.md"))
                .andExpect(jsonPath("$.sources[0].section").value("3.2 공개 조건"))
                .andExpect(jsonPath("$.sources[0].similarityScore").doesNotExist());

        verify(questionService).ask("15세 콘텐츠의 공개 조건이 뭐야?");
    }
}
