package com.contentopsagent.retrieval.model;

import java.util.List;

public record RetrievalResult(
        String question,
        int topK,
        List<RetrievedChunk> chunks,
        long embeddingLatencyMs,
        long retrievalLatencyMs
) {
}
