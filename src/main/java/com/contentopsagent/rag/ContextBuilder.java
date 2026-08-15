package com.contentopsagent.rag;

import com.contentopsagent.retrieval.model.RetrievedChunk;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ContextBuilder {

    public String build(List<RetrievedChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            RetrievedChunk chunk = chunks.get(i);
            if (i > 0) {
                builder.append("\n\n");
            }
            builder.append("[Source ").append(i + 1).append("]\n")
                    .append("Document: ").append(nullToEmpty(chunk.documentName())).append("\n")
                    .append("Section: ").append(nullToEmpty(chunk.section())).append("\n\n")
                    .append("Content:\n")
                    .append(nullToEmpty(chunk.content()));
        }
        return builder.toString();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
