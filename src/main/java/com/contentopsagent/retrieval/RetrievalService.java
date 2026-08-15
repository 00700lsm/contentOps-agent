package com.contentopsagent.retrieval;

import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.common.exception.AppException;
import com.contentopsagent.common.exception.ErrorCode;
import com.contentopsagent.retrieval.model.RetrievalResult;
import com.contentopsagent.retrieval.model.RetrievedChunk;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RetrievalService {

    private static final Logger log = LoggerFactory.getLogger(RetrievalService.class);

    private final AppProperties properties;
    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public RetrievalService(
            AppProperties properties,
            EmbeddingModel embeddingModel,
            VectorStore vectorStore
    ) {
        this.properties = properties;
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    public RetrievalResult search(String question) {
        int topK = properties.getRag().getTopK();
        long embeddingStarted = System.nanoTime();
        try {
            embeddingModel.embed(question);
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.EMBEDDING_FAILED, e);
        }
        long embeddingLatencyMs = elapsedMs(embeddingStarted);

        long retrievalStarted = System.nanoTime();
        List<Document> documents;
        try {
            documents = vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .topK(topK)
                            .build()
            );
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.VECTOR_SEARCH_FAILED, e);
        }
        long retrievalLatencyMs = elapsedMs(retrievalStarted);

        List<RetrievedChunk> chunks = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            chunks.add(toChunk(i + 1, documents.get(i)));
        }

        log.info(
                "retrieval questionLength={} topK={} retrieved={} embeddingLatencyMs={} retrievalLatencyMs={} documents={}",
                question.length(),
                topK,
                chunks.size(),
                embeddingLatencyMs,
                retrievalLatencyMs,
                chunks.stream().map(chunk -> chunk.rank() + ":" + chunk.documentName() + "@" + chunk.section()).toList()
        );
        return new RetrievalResult(question, topK, chunks, embeddingLatencyMs, retrievalLatencyMs);
    }

    private RetrievedChunk toChunk(int rank, Document document) {
        Map<String, Object> metadata = document.getMetadata();
        return new RetrievedChunk(
                rank,
                document.getText(),
                stringValue(metadata.get("documentName")),
                stringValue(metadata.get("section")),
                intValue(metadata.get("chunkIndex")),
                document.getScore()
        );
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long elapsedMs(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
