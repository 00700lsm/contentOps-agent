package com.contentopsagent.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_QUESTION(HttpStatus.BAD_REQUEST, "질문을 입력해주세요."),
    EMBEDDING_FAILED(HttpStatus.BAD_GATEWAY, "질문 Embedding 생성에 실패했습니다."),
    VECTOR_SEARCH_FAILED(HttpStatus.BAD_GATEWAY, "Vector Search에 실패했습니다."),
    LLM_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "답변 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
