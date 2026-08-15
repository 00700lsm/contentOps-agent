package com.contentopsagent.document.chunk;

import com.contentopsagent.document.model.DocumentChunk;
import com.contentopsagent.document.model.ParsedDocument;
import com.contentopsagent.document.model.ParsedSection;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SectionChunker {

    public List<DocumentChunk> chunk(ParsedDocument document) {
        List<DocumentChunk> chunks = new ArrayList<>();
        int chunkIndex = 0;
        for (ParsedSection section : document.sections()) {
            String content = section.content() == null ? "" : section.content().trim();
            if (content.isBlank()) {
                continue;
            }
            chunks.add(new DocumentChunk(
                    document.documentId() + "-" + chunkIndex,
                    document.documentId(),
                    document.documentName(),
                    section.title(),
                    chunkIndex,
                    content
            ));
            chunkIndex++;
        }
        return chunks;
    }
}
