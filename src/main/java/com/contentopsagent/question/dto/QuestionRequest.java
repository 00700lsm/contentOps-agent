package com.contentopsagent.question.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionRequest(
        @NotBlank(message = "질문을 입력해주세요.")
        String question
) {
}
