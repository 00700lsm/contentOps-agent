package com.contentopsagent.content;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class ContentCatalogLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentCatalogLoader.class);

    private final ContentIngestionService contentIngestionService;

    public ContentCatalogLoader(ContentIngestionService contentIngestionService) {
        this.contentIngestionService = contentIngestionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("loading sample contents");
        contentIngestionService.resetAndIngest();
    }
}
