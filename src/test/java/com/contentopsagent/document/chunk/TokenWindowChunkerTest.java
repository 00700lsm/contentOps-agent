package com.contentopsagent.document.chunk;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.document.model.DocumentChunk;
import com.contentopsagent.document.model.ParsedDocument;
import com.contentopsagent.document.model.ParsedSection;
import java.util.List;
import org.junit.jupiter.api.Test;

class TokenWindowChunkerTest {

    private final TokenWindowChunker chunker = new TokenWindowChunker();

    @Test
    void assignsSourceMetadataAndChunkIndex() {
        ParsedDocument document = new ParsedDocument(
                "age-rating-policy",
                "age-rating-policy.md",
                List.of(
                        new ParsedSection("3.2 공개 조건", "15세 콘텐츠는 연령 등급 검수가 완료된 이후 공개할 수 있습니다. AGE_REVIEW_REQUIRED"),
                        new ParsedSection("4. AGE_REVIEW_REQUIRED", "AGE_REVIEW_REQUIRED는 연령 등급 재검수가 필요한 상태다.")
                )
        );

        List<DocumentChunk> chunks = chunker.chunk(document, 8, 2);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.getFirst().documentId()).isEqualTo("age-rating-policy");
        assertThat(chunks.getFirst().documentName()).isEqualTo("age-rating-policy.md");
        assertThat(chunks.getFirst().section()).isEqualTo("3.2 공개 조건");
        assertThat(chunks.getFirst().chunkIndex()).isEqualTo(0);
        assertThat(chunks.getFirst().chunkId()).isEqualTo("age-rating-policy-0");
        assertThat(chunks.getLast().chunkIndex()).isEqualTo(chunks.size() - 1);
        assertThat(chunks.stream().map(DocumentChunk::section).distinct())
                .contains("3.2 공개 조건", "4. AGE_REVIEW_REQUIRED");
    }
}
