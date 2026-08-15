package com.contentopsagent.question.service;

import com.contentopsagent.common.exception.AppException;
import com.contentopsagent.common.exception.ErrorCode;
import com.contentopsagent.question.dto.QuestionResponse;
import com.contentopsagent.question.dto.SourceDto;
import com.contentopsagent.rag.RagService;
import com.contentopsagent.rag.model.RagAnswer;
import com.contentopsagent.retrieval.RetrievalService;
import com.contentopsagent.retrieval.model.RetrievalResult;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final RetrievalService retrievalService;
    private final RagService ragService;

    public QuestionService(RetrievalService retrievalService, RagService ragService) {
        this.retrievalService = retrievalService;
        this.ragService = ragService;
    }

    public QuestionResponse ask(String question) {
        String normalized = question == null ? "" : question.trim();
        if (normalized.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_QUESTION);
        }

        long started = System.nanoTime();
        RetrievalResult retrieval = retrievalService.search(normalized);
        RagAnswer generated = ragService.generate(normalized, retrieval.chunks());
        long totalLatencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        log.info(
                "question handled topK={} retrieved={} embeddingLatencyMs={} retrievalLatencyMs={} llmLatencyMs={} totalLatencyMs={} ranks={}",
                retrieval.topK(),
                retrieval.chunks().size(),
                retrieval.embeddingLatencyMs(),
                retrieval.retrievalLatencyMs(),
                generated.llmLatencyMs(),
                totalLatencyMs,
                retrieval.chunks().stream()
                        .map(chunk -> chunk.rank() + ":" + chunk.documentName() + ":" + chunk.similarityScore())
                        .toList()
        );

        return new QuestionResponse(
                generated.answer(),
                generated.sources().stream()
                        .map(source -> new SourceDto(source.document(), source.section()))
                        .toList()
        );
    }
}
