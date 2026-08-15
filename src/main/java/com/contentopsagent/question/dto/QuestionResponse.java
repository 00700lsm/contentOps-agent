package com.contentopsagent.question.dto;

import java.util.List;

public record QuestionResponse(
        String answer,
        List<SourceDto> sources
) {
}
