package com.contentopsagent.document.model;

import java.util.List;

public record ParsedDocument(
        String documentId,
        String documentName,
        List<ParsedSection> sections
) {
}
