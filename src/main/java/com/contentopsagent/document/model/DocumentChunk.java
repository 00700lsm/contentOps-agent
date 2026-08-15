package com.contentopsagent.document.model;

public record DocumentChunk(
        String chunkId,
        String documentId,
        String documentName,
        String section,
        int chunkIndex,
        String content
) {
}
