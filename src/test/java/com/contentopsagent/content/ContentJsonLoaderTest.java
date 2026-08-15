package com.contentopsagent.content;

import static org.assertj.core.api.Assertions.assertThat;

import com.contentopsagent.content.model.ContentRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContentJsonLoaderTest {

    @Test
    void loadsSampleContents() {
        List<ContentRecord> records = new ContentJsonLoader(new ObjectMapper().findAndRegisterModules())
                .load(java.nio.file.Path.of("data/contents/contents.json"));

        assertThat(records).hasSize(6);
        ContentRecord content100 = records.stream()
                .filter(record -> record.id() == 100)
                .findFirst()
                .orElseThrow();
        assertThat(content100.title()).isEqualTo("나이트 레이드");
        assertThat(content100.genre()).isEqualTo("액션");
        assertThat(content100.status()).isEqualTo("SCHEDULED");
        assertThat(content100.releaseDate()).isEqualTo(LocalDate.of(2026, 8, 20));
        assertThat(content100.metadataStatus()).isEqualTo("OK");
    }
}
