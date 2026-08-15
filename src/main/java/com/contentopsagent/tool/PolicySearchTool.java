package com.contentopsagent.tool;

import com.contentopsagent.agent.ToolCallTrace;
import com.contentopsagent.rag.ContextBuilder;
import com.contentopsagent.retrieval.RetrievalService;
import com.contentopsagent.retrieval.model.RetrievalResult;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class PolicySearchTool {

    public static final String NAME = "search_policy_documents";

    private final RetrievalService retrievalService;
    private final ContextBuilder contextBuilder;
    private final ToolCallTrace toolCallTrace;

    public PolicySearchTool(
            RetrievalService retrievalService,
            ContextBuilder contextBuilder,
            ToolCallTrace toolCallTrace
    ) {
        this.retrievalService = retrievalService;
        this.contextBuilder = contextBuilder;
        this.toolCallTrace = toolCallTrace;
    }

    @Tool(name = NAME, description = "운영 정책 문서를 Vector Search로 찾는다. 콘텐츠 목록이나 콘텐츠 상세를 조회하지 않는다.")
    public String searchPolicyDocuments(
            @ToolParam(description = "정책 문서에서 찾을 질문") String question
    ) {
        RetrievalResult result = retrievalService.search(question);
        String context = contextBuilder.build(result.chunks());
        String output = context.isBlank() ? "관련 정책 문서를 찾지 못했습니다." : context;
        toolCallTrace.record(NAME, question, output);
        return output;
    }
}
