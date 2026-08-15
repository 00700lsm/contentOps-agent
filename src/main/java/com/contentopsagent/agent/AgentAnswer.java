package com.contentopsagent.agent;

import com.contentopsagent.question.dto.SourceDto;
import java.util.List;

public record AgentAnswer(
        String answer,
        List<RecordedToolCall> toolCalls,
        List<SourceDto> sources,
        long latencyMs
) {
    public List<String> toolNames() {
        return toolCalls.stream().map(RecordedToolCall::name).toList();
    }
}
