package com.contentopsagent.document;

import com.contentopsagent.common.config.AppProperties;
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
    private final TokenWindowChunker chunker;
    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    public DocumentIngestionService(
            AppProperties properties,
            DocumentLoader documentLoader,
            MarkdownDocumentParser parser,
            TokenWindowChunker chunker,
            VectorStore vectorStore,
            JdbcTemplate jdbcTemplate
    ) {
        this.properties = properties;
        this.documentLoader = documentLoader;
        this.parser = parser;
        this.chunker = chunker;
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
    }

    public int resetAndIngest() {
        reset();
        List<SourceDocument> documents = documentLoader.load(Path.of(properties.getDocuments().getPath()));
        List<DocumentChunk> chunks = new ArrayList<>();
        for (SourceDocument document : documents) {
            ParsedDocument parsed = parser.parse(document);
            chunks.addAll(chunker.chunk(
                    parsed,
                    properties.getRag().getChunkSize(),
                    properties.getRag().getChunkOverlap()
            ));
        }
        List<Document> aiDocuments = chunks.stream().map(this::toAiDocument).toList();
        if (!aiDocuments.isEmpty()) {
            vectorStore.add(aiDocuments);
        }
        log.info(
                "ingested documents={} chunks={} dataset={} chunkSize={} overlap={}",
                documents.size(),
                chunks.size(),
                properties.getDocuments().getPath(),
                properties.getRag().getChunkSize(),
                properties.getRag().getChunkOverlap()
        );
        return chunks.size();
    }

    public void reset() {
        jdbcTemplate.update("DELETE FROM vector_store");
        log.info("reset vector store");
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
