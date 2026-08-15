package com.contentopsagent.content;

import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.content.model.ContentRecord;
import java.nio.file.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(ContentIngestionService.class);

    private final AppProperties properties;
    private final ContentJsonLoader jsonLoader;
    private final ContentRepository contentRepository;

    public ContentIngestionService(
            AppProperties properties,
            ContentJsonLoader jsonLoader,
            ContentRepository contentRepository
    ) {
        this.properties = properties;
        this.jsonLoader = jsonLoader;
        this.contentRepository = contentRepository;
    }

    public int resetAndIngest() {
        contentRepository.ensureSchema();
        List<ContentRecord> records = jsonLoader.load(Path.of(properties.getContents().getPath()));
        contentRepository.replaceAll(records);
        log.info("ingested contents count={} path={}", records.size(), properties.getContents().getPath());
        return records.size();
    }
}
