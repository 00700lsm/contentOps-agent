package com.contentopsagent.evaluation.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EvaluationDatasetLoader {

    private final ObjectMapper objectMapper;

    public EvaluationDatasetLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<EvaluationQuery> load(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Evaluation Dataset이 없습니다: " + path.toAbsolutePath());
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<EvaluationQuery> queries = new ArrayList<>();
            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }
                queries.add(objectMapper.readValue(line, EvaluationQuery.class));
            }
            return queries;
        } catch (IOException e) {
            throw new IllegalStateException("Evaluation Dataset을 읽지 못했습니다: " + path, e);
        }
    }
}
