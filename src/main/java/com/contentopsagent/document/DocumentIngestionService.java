package com.contentopsagent.document;

import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.document.chunk.SectionChunker;
import com.contentopsagent.document.chunk.TokenWindowChunker;
import com.contentopsagent.document.loader.DocumentLoader;
import com.contentopsagent.document.loader.MarkdownDocumentParser;
import com.contentopsagent.document.model.DocumentChunk;
import com.contentopsagent.document.model.ParsedDocument;
import com.contentopsagent.document.model.SourceDocument;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final AppProperties properties;
    private final DocumentLoader documentLoader;
    private final MarkdownDocumentParser parser;
    private final TokenWindowChunker tokenWindowChunker;
    private final SectionChunker sectionChunker;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;
    private IngestStats lastStats;

    public DocumentIngestionService(
            AppProperties properties,
            DocumentLoader documentLoader,
            MarkdownDocumentParser parser,
            TokenWindowChunker tokenWindowChunker,
            SectionChunker sectionChunker,
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate
    ) {
        this.properties = properties;
        this.documentLoader = documentLoader;
        this.parser = parser;
        this.tokenWindowChunker = tokenWindowChunker;
        this.sectionChunker = sectionChunker;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public IngestStats resetAndIngest() {
        reset();
        List<SourceDocument> documents = documentLoader.load(Path.of(properties.getDocuments().getPath()));
        String strategy = chunkingStrategy();
        List<DocumentChunk> chunks = new ArrayList<>();
        for (SourceDocument document : documents) {
            ParsedDocument parsed = parser.parse(document);
            chunks.addAll(chunk(parsed, strategy));
        }
        List<Document> aiDocuments = chunks.stream().map(this::toAiDocument).toList();
        if (!aiDocuments.isEmpty()) {
            vectorStore.add(aiDocuments);
        }
        int averageChars = chunks.isEmpty()
                ? 0
                : (int) Math.round(chunks.stream().mapToInt(chunk -> chunk.content().length()).average().orElse(0));
        lastStats = new IngestStats(
                strategy,
                properties.getRag().getChunkSize(),
                properties.getRag().getChunkOverlap(),
                documents.size(),
                chunks.size(),
                averageChars
        );
        log.info(
                "ingested documents={} chunks={} avgChunkChars={} strategy={} chunkSize={} overlap={}",
                lastStats.documentCount(),
                lastStats.chunkCount(),
                lastStats.averageChunkChars(),
                lastStats.chunkingStrategy(),
                lastStats.chunkSize(),
                lastStats.chunkOverlap()
        );
        return lastStats;
    }

    public IngestStats lastStats() {
        return lastStats;
    }

    public void reset() {
        jdbcTemplate.update("DELETE FROM vector_store");
        log.info("reset vector store");
    }

    private List<DocumentChunk> chunk(ParsedDocument parsed, String strategy) {
        if ("section".equals(strategy)) {
            return sectionChunker.chunk(parsed);
        }
        return tokenWindowChunker.chunk(
                parsed,
                properties.getRag().getChunkSize(),
                properties.getRag().getChunkOverlap()
        );
    }

    private String chunkingStrategy() {
        String strategy = properties.getRag().getChunkingStrategy();
        if (strategy == null || strategy.isBlank()) {
            return "fixed";
        }
        return strategy.trim().toLowerCase(Locale.ROOT);
    }

    private Document toAiDocument(DocumentChunk chunk) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", chunk.documentId());
        metadata.put("documentName", chunk.documentName());
        metadata.put("section", chunk.section());
        metadata.put("chunkId", chunk.chunkId());
        metadata.put("chunkIndex", chunk.chunkIndex());
        return new Document(UUID.randomUUID().toString(), chunk.content(), metadata);
    }
}
