package com.contentopsagent.tool;

import com.contentopsagent.agent.ToolCallTrace;
import com.contentopsagent.content.ContentQueryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ContentDetailTool {

    public static final String NAME = "get_content_detail";

    private final ContentQueryService contentQueryService;
    private final ObjectMapper objectMapper;
    private final ToolCallTrace toolCallTrace;

    public ContentDetailTool(
            ContentQueryService contentQueryService,
            ObjectMapper objectMapper,
            ToolCallTrace toolCallTrace
    ) {
        this.contentQueryService = contentQueryService;
        this.objectMapper = objectMapper;
        this.toolCallTrace = toolCallTrace;
    }

    @Tool(name = NAME, description = "콘텐츠 ID로 상세 정보를 조회한다. 정책 문서를 검색하지 않는다.")
    public String getContentDetail(
            @ToolParam(description = "콘텐츠 ID") long contentId
    ) {
        String output = contentQueryService.findById(contentId)
                .map(record -> {
                    try {
                        return objectMapper.writeValueAsString(record);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException("콘텐츠 상세를 직렬화하지 못했습니다.", e);
                    }
                })
                .orElse("콘텐츠를 찾지 못했습니다. id=" + contentId);
        toolCallTrace.record(NAME, String.valueOf(contentId), output);
        return output;
    }
}
