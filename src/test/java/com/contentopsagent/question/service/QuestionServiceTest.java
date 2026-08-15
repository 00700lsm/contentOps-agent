package com.contentopsagent.question.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contentopsagent.common.exception.AppException;
import com.contentopsagent.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class QuestionServiceTest {

    private final QuestionService questionService = new QuestionService(null);

    @Test
    void rejectsNullOrBlankQuestion() {
        assertThatThrownBy(() -> questionService.ask(null))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).errorCode())
                .isEqualTo(ErrorCode.INVALID_QUESTION);
        assertThatThrownBy(() -> questionService.ask("   "))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).errorCode())
                .isEqualTo(ErrorCode.INVALID_QUESTION);
    }
}
