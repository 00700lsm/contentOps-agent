package com.contentopsagent.question.dto;

import java.util.List;

public record QuestionResponse(
        String answer,
        List<SourceDto> sources,
        List<String> tools
) {
    public QuestionResponse(String answer, List<SourceDto> sources) {
        this(answer, sources, List.of());
    }
}
