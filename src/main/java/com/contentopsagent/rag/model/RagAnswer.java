package com.contentopsagent.rag.model;

import java.util.List;

public record RagAnswer(
        String answer,
        List<AnswerSource> sources,
        long llmLatencyMs
) {
}
