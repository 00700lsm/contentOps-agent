package com.contentopsagent.content;

import com.contentopsagent.content.model.ContentRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ContentJsonLoader {

    private final ObjectMapper objectMapper;

    public ContentJsonLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ContentRecord> load(Path path) {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Content Dataset이 없습니다: " + path.toAbsolutePath());
        }
        try {
            return objectMapper.readValue(path.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalStateException("Content Dataset을 읽지 못했습니다: " + path, e);
        }
    }
}
