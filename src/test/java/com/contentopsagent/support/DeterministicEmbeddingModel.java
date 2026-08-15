package com.contentopsagent.support;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

public class DeterministicEmbeddingModel implements EmbeddingModel {

    private final int dimensions;

    public DeterministicEmbeddingModel(int dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();
        List<String> inputs = request.getInstructions();
        for (int i = 0; i < inputs.size(); i++) {
            embeddings.add(new Embedding(embed(inputs.get(i)), i));
        }
        return new EmbeddingResponse(embeddings);
    }

    @Override
    public float[] embed(Document document) {
        return embed(document.getText());
    }

    @Override
    public float[] embed(String text) {
        float[] vector = new float[dimensions];
        if (text == null || text.isBlank()) {
            vector[0] = 1.0f;
            return vector;
        }
        String normalized = text.toLowerCase();
        for (String token : normalized.split("[^a-z0-9가-힣_\\-]+")) {
            if (token.isBlank()) {
                continue;
            }
            int index = Math.floorMod(token.hashCode(), dimensions);
            vector[index] += 1.0f;
            byte[] bytes = token.getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < bytes.length; i++) {
                vector[Math.floorMod(bytes[i] + i, dimensions)] += 0.05f;
            }
        }
        double norm = 0.0;
        for (float value : vector) {
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        if (norm == 0.0) {
            vector[0] = 1.0f;
            return vector;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / norm);
        }
        return vector;
    }

    @Override
    public int dimensions() {
        return dimensions;
    }
}
