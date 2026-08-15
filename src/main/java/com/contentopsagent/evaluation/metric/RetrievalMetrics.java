package com.contentopsagent.evaluation.metric;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class RetrievalMetrics {

    private RetrievalMetrics() {
    }

    public static boolean hit(List<String> expectedDocuments, List<String> actualDocuments) {
        Set<String> expected = normalize(expectedDocuments);
        if (expected.isEmpty()) {
            return false;
        }
        return actualDocuments.stream().map(RetrievalMetrics::normalize).anyMatch(expected::contains);
    }

    public static double recall(List<String> expectedDocuments, List<String> actualDocuments) {
        Set<String> expected = normalize(expectedDocuments);
        if (expected.isEmpty()) {
            return 0.0;
        }
        Set<String> actual = actualDocuments.stream()
                .map(RetrievalMetrics::normalize)
                .collect(Collectors.toSet());
        long found = expected.stream().filter(actual::contains).count();
        return (double) found / expected.size();
    }

    public static int firstExpectedRank(List<String> expectedDocuments, List<String> actualDocuments) {
        Set<String> expected = normalize(expectedDocuments);
        if (expected.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < actualDocuments.size(); i++) {
            if (expected.contains(normalize(actualDocuments.get(i)))) {
                return i + 1;
            }
        }
        return -1;
    }

    public static double reciprocalRank(List<String> expectedDocuments, List<String> actualDocuments) {
        int rank = firstExpectedRank(expectedDocuments, actualDocuments);
        if (rank <= 0) {
            return 0.0;
        }
        return 1.0 / rank;
    }

    private static Set<String> normalize(List<String> values) {
        if (values == null) {
            return Set.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(RetrievalMetrics::normalize)
                .collect(Collectors.toSet());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
