package com.contentopsagent.retrieval.model;

public record RetrievedChunk(
        int rank,
        String content,
        String documentName,
        String section,
        int chunkIndex,
        Double similarityScore
) {
}
