package com.contentopsagent.document.model;

public record SourceDocument(
        String documentId,
        String documentName,
        String content
) {
}
