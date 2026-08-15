package com.contentopsagent.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Documents documents = new Documents();
    private final Rag rag = new Rag();
    private final Ai ai = new Ai();
    private final Ingest ingest = new Ingest();
    private final Evaluate evaluate = new Evaluate();
    private boolean exitAfterJob = true;

    public Documents getDocuments() {
        return documents;
    }

    public Rag getRag() {
        return rag;
    }

    public Ai getAi() {
        return ai;
    }

    public Ingest getIngest() {
        return ingest;
    }

    public Evaluate getEvaluate() {
        return evaluate;
    }

    public boolean isExitAfterJob() {
        return exitAfterJob;
    }

    public void setExitAfterJob(boolean exitAfterJob) {
        this.exitAfterJob = exitAfterJob;
    }

    public static class Documents {
        private String path = "data/documents";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class Rag {
        private int topK = 5;
        private int chunkSize = 500;
        private int chunkOverlap = 50;

        public int getTopK() {
            return topK;
        }

        public void setTopK(int topK) {
            this.topK = topK;
        }

        public int getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
        }

        public int getChunkOverlap() {
            return chunkOverlap;
        }

        public void setChunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
        }
    }

    public static class Ai {
        private String embeddingModel = "nomic-embed-text";
        private String chatModel = "llama3.2";

        public String getEmbeddingModel() {
            return embeddingModel;
        }

        public void setEmbeddingModel(String embeddingModel) {
            this.embeddingModel = embeddingModel;
        }

        public String getChatModel() {
            return chatModel;
        }

        public void setChatModel(String chatModel) {
            this.chatModel = chatModel;
        }
    }

    public static class Ingest {
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Evaluate {
        private boolean enabled = false;
        private String datasetPath = "evaluation/datasets/retrieval.jsonl";
        private String resultsDir = "evaluation/results";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getDatasetPath() {
            return datasetPath;
        }

        public void setDatasetPath(String datasetPath) {
            this.datasetPath = datasetPath;
        }

        public String getResultsDir() {
            return resultsDir;
        }

        public void setResultsDir(String resultsDir) {
            this.resultsDir = resultsDir;
        }
    }
}
