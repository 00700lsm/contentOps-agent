package com.contentopsagent.evaluation.runner;

import com.contentopsagent.agent.AgentAnswer;
import com.contentopsagent.agent.AgentService;
import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.evaluation.dataset.EvaluationDatasetLoader;
import com.contentopsagent.evaluation.dataset.EvaluationQuery;
import com.contentopsagent.evaluation.metric.ToolSelectionMetrics;
import com.contentopsagent.evaluation.result.AgentEvaluationResult;
import com.contentopsagent.evaluation.result.AgentEvaluationSummary;
import com.contentopsagent.evaluation.result.AgentQueryEvaluationResult;
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
public class AgentEvaluationRunner {

    private static final Logger log = LoggerFactory.getLogger(AgentEvaluationRunner.class);
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
            .withZone(ZoneOffset.UTC);

    private final AppProperties properties;
    private final EvaluationDatasetLoader datasetLoader;
    private final AgentService agentService;
    private final ObjectMapper objectMapper;

    public AgentEvaluationRunner(
            AppProperties properties,
            EvaluationDatasetLoader datasetLoader,
            AgentService agentService,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.datasetLoader = datasetLoader;
        this.agentService = agentService;
        this.objectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public AgentEvaluationResult run() {
        Path datasetPath = Path.of(properties.getEvaluate().getAgentDatasetPath());
        List<EvaluationQuery> queries = datasetLoader.load(datasetPath);
        List<AgentQueryEvaluationResult> results = new ArrayList<>();
        for (EvaluationQuery query : queries) {
            results.add(evaluate(query));
        }
        AgentEvaluationResult result = new AgentEvaluationResult(
                Instant.now(),
                datasetPath.toString(),
                properties.getAi().getChatModel(),
                "Simple Tool Calling",
                summarize(results),
                results
        );
        Path output = write(result);
        log.info(
                "agent evaluation complete queries={} accuracy={} sequenceAccuracy={} output={}",
                result.metrics().queryCount(),
                result.metrics().toolSelectionAccuracy(),
                result.metrics().sequenceAccuracy(),
                output.toAbsolutePath()
        );
        return result;
    }

    private AgentQueryEvaluationResult evaluate(EvaluationQuery query) {
        AgentAnswer answer = agentService.ask(query.question());
        List<String> expected = query.expectedTools();
        List<String> actual = answer.toolNames();
        return new AgentQueryEvaluationResult(
                query.id(),
                query.category(),
                query.question(),
                expected,
                actual,
                ToolSelectionMetrics.missing(expected, actual),
                ToolSelectionMetrics.extra(expected, actual),
                ToolSelectionMetrics.setMatch(expected, actual),
                ToolSelectionMetrics.sequenceMatch(expected, actual),
                ToolSelectionMetrics.classify(expected, actual),
                answer.answer(),
                answer.latencyMs()
        );
    }

    private AgentEvaluationSummary summarize(List<AgentQueryEvaluationResult> results) {
        int n = results.size();
        long match = results.stream().filter(result -> "MATCH".equals(result.failureType())).count();
        long setMatch = results.stream().filter(AgentQueryEvaluationResult::setMatch).count();
        long wrongOrder = results.stream().filter(result -> "WRONG_ORDER".equals(result.failureType())).count();
        long missing = results.stream().filter(result -> "MISSING_TOOL".equals(result.failureType())).count();
        long unnecessary = results.stream().filter(result -> "UNNECESSARY_TOOL".equals(result.failureType())).count();
        long wrong = results.stream().filter(result -> "WRONG_TOOL".equals(result.failureType())).count();
        double avgLatency = n == 0 ? 0 : results.stream().mapToLong(AgentQueryEvaluationResult::latencyMs).average().orElse(0);
        return new AgentEvaluationSummary(
                n,
                n == 0 ? 0 : (double) setMatch / n,
                n == 0 ? 0 : (double) match / n,
                (int) match,
                (int) wrongOrder,
                (int) missing,
                (int) unnecessary,
                (int) wrong,
                avgLatency
        );
    }

    private Path write(AgentEvaluationResult result) {
        try {
            Path directory = Path.of(properties.getEvaluate().getResultsDir());
            Files.createDirectories(directory);
            Path output = directory.resolve("agent-" + FILE_TIME.format(result.executedAt()) + ".json");
            objectMapper.writeValue(output.toFile(), result);
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("Agent Evaluation 결과를 저장하지 못했습니다.", e);
        }
    }
}
