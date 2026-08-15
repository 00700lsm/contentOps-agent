package com.contentopsagent.document;

public record IngestStats(
        String chunkingStrategy,
        int chunkSize,
        int chunkOverlap,
        int documentCount,
        int chunkCount,
        int averageChunkChars
) {
}
