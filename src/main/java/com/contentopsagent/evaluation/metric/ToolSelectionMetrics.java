package com.contentopsagent.evaluation.metric;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ToolSelectionMetrics {

    private ToolSelectionMetrics() {
    }

    public static Set<String> unique(List<String> tools) {
        if (tools == null) {
            return Set.of();
        }
        return new LinkedHashSet<>(tools);
    }

    public static boolean setMatch(List<String> expected, List<String> actual) {
        return unique(expected).equals(unique(actual));
    }

    public static boolean sequenceMatch(List<String> expected, List<String> actual) {
        List<String> expectedList = expected == null ? List.of() : expected;
        List<String> actualList = actual == null ? List.of() : actual;
        return expectedList.equals(actualList);
    }

    public static List<String> missing(List<String> expected, List<String> actual) {
        Set<String> actualSet = unique(actual);
        List<String> missing = new ArrayList<>();
        for (String tool : unique(expected)) {
            if (!actualSet.contains(tool)) {
                missing.add(tool);
            }
        }
        return missing;
    }

    public static List<String> extra(List<String> expected, List<String> actual) {
        Set<String> expectedSet = unique(expected);
        List<String> extra = new ArrayList<>();
        for (String tool : unique(actual)) {
            if (!expectedSet.contains(tool)) {
                extra.add(tool);
            }
        }
        return extra;
    }

    public static String classify(List<String> expected, List<String> actual) {
        if (sequenceMatch(expected, actual)) {
            return "MATCH";
        }
        if (setMatch(expected, actual)) {
            return "WRONG_ORDER";
        }
        List<String> missingTools = missing(expected, actual);
        List<String> extraTools = extra(expected, actual);
        if (!missingTools.isEmpty() && extraTools.isEmpty()) {
            return "MISSING_TOOL";
        }
        if (missingTools.isEmpty() && !extraTools.isEmpty()) {
            return "UNNECESSARY_TOOL";
        }
        return "WRONG_TOOL";
    }
}
