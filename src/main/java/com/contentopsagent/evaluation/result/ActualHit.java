package com.contentopsagent.evaluation.result;

public record ActualHit(
        int rank,
        String documentName,
        String section,
        int chunkIndex,
        Double similarityScore
) {
}
