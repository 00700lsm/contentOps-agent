package com.contentopsagent.evaluation.runner;

import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.evaluation.dataset.EvaluationDatasetLoader;
import com.contentopsagent.evaluation.dataset.EvaluationQuery;
import com.contentopsagent.evaluation.metric.RetrievalMetrics;
import com.contentopsagent.evaluation.result.ActualHit;
import com.contentopsagent.evaluation.result.EvaluationResult;
import com.contentopsagent.evaluation.result.EvaluationSummary;
import com.contentopsagent.evaluation.result.QueryEvaluationResult;
import com.contentopsagent.retrieval.RetrievalService;
import com.contentopsagent.retrieval.model.RetrievalResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EvaluationRunner {

    private static final Logger log = LoggerFactory.getLogger(EvaluationRunner.class);
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);

    private final AppProperties properties;
    private final EvaluationDatasetLoader datasetLoader;
    private final RetrievalService retrievalService;
    private final ObjectMapper objectMapper;

    public EvaluationRunner(
            AppProperties properties,
            EvaluationDatasetLoader datasetLoader,
            RetrievalService retrievalService,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.datasetLoader = datasetLoader;
        this.retrievalService = retrievalService;
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public EvaluationResult run() {
        Path datasetPath = Path.of(properties.getEvaluate().getDatasetPath());
        List<EvaluationQuery> queries = datasetLoader.load(datasetPath);
        List<QueryEvaluationResult> queryResults = new ArrayList<>();
        for (EvaluationQuery query : queries) {
            queryResults.add(evaluate(query));
        }

        EvaluationResult result = new EvaluationResult(
                Instant.now(),
                properties.getDocuments().getPath(),
                datasetPath.toString(),
                properties.getAi().getEmbeddingModel(),
                properties.getAi().getChatModel(),
                properties.getRag().getChunkSize(),
                properties.getRag().getChunkOverlap(),
                properties.getRag().getTopK(),
                "Vector Search Only",
                EvaluationSummary.from(queryResults),
                queryResults
        );
        Path output = write(result);
        log.info(
                "evaluation complete queries={} scored={} hitRate@K={} recall@K={} mrr={} output={}",
                result.metrics().queryCount(),
                result.metrics().scoredQueryCount(),
                result.metrics().hitRateAtK(),
                result.metrics().recallAtK(),
                result.metrics().mrr(),
                output.toAbsolutePath()
        );
        return result;
    }

    private QueryEvaluationResult evaluate(EvaluationQuery query) {
        RetrievalResult retrieval = retrievalService.search(query.question());
        List<String> actualDocuments = retrieval.chunks().stream()
                .map(chunk -> chunk.documentName())
                .toList();
        int rank = RetrievalMetrics.firstExpectedRank(query.expectedDocuments(), actualDocuments);
        return new QueryEvaluationResult(
                query.id(),
                query.category(),
                query.question(),
                query.expectedDocuments(),
                query.answerable(),
                retrieval.chunks().stream()
                        .map(chunk -> new ActualHit(
                                chunk.rank(),
                                chunk.documentName(),
                                chunk.section(),
                                chunk.chunkIndex(),
                                chunk.similarityScore()
                        ))
                        .toList(),
                rank > 0 ? rank : null,
                RetrievalMetrics.hit(query.expectedDocuments(), actualDocuments),
                RetrievalMetrics.recall(query.expectedDocuments(), actualDocuments),
                RetrievalMetrics.reciprocalRank(query.expectedDocuments(), actualDocuments),
                retrieval.retrievalLatencyMs()
        );
    }

    private Path write(EvaluationResult result) {
        try {
            Path directory = Path.of(properties.getEvaluate().getResultsDir());
            Files.createDirectories(directory);
            Path output = directory.resolve("retrieval-" + FILE_TIME.format(result.executedAt()) + ".json");
            objectMapper.writeValue(output.toFile(), result);
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("Evaluation 결과를 저장하지 못했습니다.", e);
        }
    }
}
