package com.contentopsagent.document.loader;

import com.contentopsagent.document.model.SourceDocument;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class DocumentLoader {

    public List<SourceDocument> load(Path directory) {
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("문서 디렉터리가 없습니다: " + directory.toAbsolutePath());
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .map(this::read)
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("문서를 읽지 못했습니다: " + directory, e);
        }
    }

    private SourceDocument read(Path path) {
        try {
            String fileName = path.getFileName().toString();
            String documentId = fileName.replaceFirst("\\.md$", "");
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return new SourceDocument(documentId, fileName, content);
        } catch (IOException e) {
            throw new IllegalStateException("문서를 읽지 못했습니다: " + path, e);
        }
    }
}
