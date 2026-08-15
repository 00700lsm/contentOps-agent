package com.contentopsagent.document.chunk;

import com.contentopsagent.document.model.DocumentChunk;
import com.contentopsagent.document.model.ParsedDocument;
import com.contentopsagent.document.model.ParsedSection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class TokenWindowChunker {

    private static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9_\\-]+|[가-힣]|\\S");

    public List<DocumentChunk> chunk(ParsedDocument document, int chunkSize, int overlap) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException("overlap must be >= 0 and < chunkSize");
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        int chunkIndex = 0;
        for (ParsedSection section : document.sections()) {
            List<DocumentChunk> sectionChunks = chunkSectionWindows(document, section, chunkIndex, chunkSize, overlap);
            chunks.addAll(sectionChunks);
            chunkIndex += sectionChunks.size();
        }
        return chunks;
    }

    private List<DocumentChunk> chunkSectionWindows(
            ParsedDocument document,
            ParsedSection section,
            int startingIndex,
            int chunkSize,
            int overlap
    ) {
        String text = section.content() == null ? "" : section.content().trim();
        if (text.isBlank()) {
            return List.of();
        }
        List<TokenSpan> tokens = tokenize(text);
        if (tokens.isEmpty()) {
            return List.of();
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        int step = chunkSize - overlap;
        int chunkIndex = startingIndex;
        for (int start = 0; start < tokens.size(); start += step) {
            int end = Math.min(start + chunkSize, tokens.size());
            String content = text.substring(tokens.get(start).start(), tokens.get(end - 1).end()).trim();
            String chunkId = document.documentId() + "-" + chunkIndex;
            chunks.add(new DocumentChunk(
                    chunkId,
                    document.documentId(),
                    document.documentName(),
                    section.title(),
                    chunkIndex,
                    content
            ));
            chunkIndex++;
            if (end == tokens.size()) {
                break;
            }
        }
        return chunks;
    }

    private List<TokenSpan> tokenize(String text) {
        Matcher matcher = TOKEN.matcher(text);
        List<TokenSpan> tokens = new ArrayList<>();
        while (matcher.find()) {
            tokens.add(new TokenSpan(matcher.start(), matcher.end()));
        }
        return tokens;
    }

    private record TokenSpan(int start, int end) {
    }
}
