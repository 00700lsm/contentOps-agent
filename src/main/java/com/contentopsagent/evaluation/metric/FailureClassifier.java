package com.contentopsagent.evaluation.metric;

public final class FailureClassifier {

    public static final int RANKING_FAILURE_MIN_RANK = 3;

    private FailureClassifier() {
    }

    public static String classify(
            boolean answerable,
            boolean hit,
            Integer expectedDocumentRank,
            String answer
    ) {
        if (!answerable) {
            if (looksLikeNoAnswer(answer)) {
                return "NONE";
            }
            return "NO_ANSWER";
        }
        if (!hit) {
            return "RETRIEVAL";
        }
        if (expectedDocumentRank != null && expectedDocumentRank >= RANKING_FAILURE_MIN_RANK) {
            return "RANKING";
        }
        return "NEEDS_GENERATION_REVIEW";
    }

    public static boolean looksLikeNoAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            return true;
        }
        String normalized = answer.replaceAll("\\s+", "");
        return normalized.contains("확인할수없")
                || normalized.contains("찾을수없")
                || normalized.contains("제공된문서에서는확인할수없")
                || normalized.contains("판단할수없");
    }
}
