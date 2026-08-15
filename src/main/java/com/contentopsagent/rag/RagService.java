package com.contentopsagent.rag;

import com.contentopsagent.common.exception.AppException;
import com.contentopsagent.common.exception.ErrorCode;
import com.contentopsagent.rag.model.AnswerSource;
import com.contentopsagent.rag.model.RagAnswer;
import com.contentopsagent.rag.prompt.BaselinePrompt;
import com.contentopsagent.retrieval.model.RetrievedChunk;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final ContextBuilder contextBuilder;
    private final BaselinePrompt baselinePrompt;
    private final ChatModel chatModel;

    public RagService(
            ContextBuilder contextBuilder,
            BaselinePrompt baselinePrompt,
            ChatModel chatModel
    ) {
        this.contextBuilder = contextBuilder;
        this.baselinePrompt = baselinePrompt;
        this.chatModel = chatModel;
    }

    public RagAnswer generate(String question, List<RetrievedChunk> chunks) {
        String context = contextBuilder.build(chunks);
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(baselinePrompt.systemPrompt()),
                new UserMessage(baselinePrompt.userPrompt(question, context))
        ));

        long started = System.nanoTime();
        ChatResponse response;
        try {
            response = chatModel.call(prompt);
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.LLM_GENERATION_FAILED, e);
        }
        long llmLatencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        String answer = extractAnswer(response);
        List<AnswerSource> sources = sourcesFromChunks(chunks);
        log.info("generation sourceCount={} llmLatencyMs={} answerLength={}", sources.size(), llmLatencyMs, answer.length());
        return new RagAnswer(answer, sources, llmLatencyMs);
    }

    private String extractAnswer(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            throw new AppException(ErrorCode.LLM_GENERATION_FAILED);
        }
        String text = response.getResult().getOutput().getText();
        if (text == null || text.isBlank()) {
            throw new AppException(ErrorCode.LLM_GENERATION_FAILED);
        }
        return text.trim();
    }

    private List<AnswerSource> sourcesFromChunks(List<RetrievedChunk> chunks) {
        Set<String> seen = new LinkedHashSet<>();
        List<AnswerSource> sources = new ArrayList<>();
        for (RetrievedChunk chunk : chunks) {
            String key = chunk.documentName() + "|" + chunk.section();
            if (seen.add(key)) {
                sources.add(new AnswerSource(chunk.documentName(), chunk.section()));
            }
        }
        return sources;
    }
}
