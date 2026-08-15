package com.contentopsagent.runtime;

import com.contentopsagent.common.config.AppProperties;
import com.contentopsagent.document.DocumentIngestionService;
import com.contentopsagent.evaluation.runner.EvaluationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class JobRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JobRunner.class);

    private final AppProperties properties;
    private final DocumentIngestionService ingestionService;
    private final EvaluationRunner evaluationRunner;
    private final ApplicationContext applicationContext;

    public JobRunner(
            AppProperties properties,
            DocumentIngestionService ingestionService,
            EvaluationRunner evaluationRunner,
            ApplicationContext applicationContext
    ) {
        this.properties = properties;
        this.ingestionService = ingestionService;
        this.evaluationRunner = evaluationRunner;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean ingest = properties.getIngest().isEnabled();
        boolean evaluate = properties.getEvaluate().isEnabled();
        if (!ingest && !evaluate) {
            return;
        }
        if (ingest) {
            log.info("starting document ingestion");
            ingestionService.resetAndIngest();
        }
        if (evaluate) {
            log.info("starting retrieval evaluation");
            evaluationRunner.run();
        }
        if (properties.isExitAfterJob()) {
            SpringApplication.exit(applicationContext, () -> 0);
        }
    }
}
