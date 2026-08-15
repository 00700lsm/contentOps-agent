package com.contentopsagent.document.chunk;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.document.model.DocumentChunk;
import com.contentopsagent.document.model.ParsedDocument;
import com.contentopsagent.document.model.ParsedSection;
import java.util.List;
import org.junit.jupiter.api.Test;

class SectionChunkerTest {

    private final SectionChunker chunker = new SectionChunker();

    @Test
    void createsOneChunkPerSection() {
        ParsedDocument document = new ParsedDocument(
                "metadata-guide",
                "metadata-guide.md",
                List.of(
                        new ParsedSection("2. M-03 오류", "`M-03`은 필수 메타데이터 누락 오류다."),
                        new ParsedSection("3. 기타 오류", "M-03: 필수 메타데이터 누락")
                )
        );

        List<DocumentChunk> chunks = chunker.chunk(document);

        assertThat(chunks).hasSize(2);
        assertThat(chunks.getFirst().section()).isEqualTo("2. M-03 오류");
        assertThat(chunks.getFirst().content()).contains("필수 메타데이터 누락 오류");
        assertThat(chunks.getLast().section()).isEqualTo("3. 기타 오류");
        assertThat(chunks.getLast().chunkIndex()).isEqualTo(1);
    }
}
