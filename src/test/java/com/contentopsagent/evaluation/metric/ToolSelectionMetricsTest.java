package com.contentopsagent.evaluation.metric;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ToolSelectionMetricsTest {

    @Test
    void classifiesMatchWrongOrderMissingAndExtra() {
        assertThat(ToolSelectionMetrics.classify(
                List.of("get_content_detail"),
                List.of("get_content_detail")
        )).isEqualTo("MATCH");

        assertThat(ToolSelectionMetrics.classify(
                List.of("get_content_detail", "search_policy_documents"),
                List.of("search_policy_documents", "get_content_detail")
        )).isEqualTo("WRONG_ORDER");

        assertThat(ToolSelectionMetrics.classify(
                List.of("get_content_detail", "search_policy_documents"),
                List.of("get_content_detail")
        )).isEqualTo("MISSING_TOOL");

        assertThat(ToolSelectionMetrics.classify(
                List.of("search_policy_documents"),
                List.of("search_policy_documents", "search_contents")
        )).isEqualTo("UNNECESSARY_TOOL");

        assertThat(ToolSelectionMetrics.classify(
                List.of("get_content_detail"),
                List.of("search_policy_documents")
        )).isEqualTo("WRONG_TOOL");
    }
}
