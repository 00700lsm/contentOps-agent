package com.contentopsagent.question.service;

import com.contentopsagent.agent.AgentAnswer;
import com.contentopsagent.agent.AgentService;
import com.contentopsagent.common.exception.AppException;
import com.contentopsagent.common.exception.ErrorCode;
import com.contentopsagent.question.dto.QuestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final AgentService agentService;

    public QuestionService(AgentService agentService) {
        this.agentService = agentService;
    }

    public QuestionResponse ask(String question) {
        String normalized = question == null ? "" : question.trim();
        if (normalized.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_QUESTION);
        }

        AgentAnswer generated = agentService.ask(normalized);
        log.info(
                "question handled tools={} latencyMs={} answerLength={}",
                generated.toolNames(),
                generated.latencyMs(),
                generated.answer().length()
        );
        return new QuestionResponse(generated.answer(), generated.sources(), generated.toolNames());
    }
}
