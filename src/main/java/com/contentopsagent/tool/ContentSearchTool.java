package com.contentopsagent.tool;

import com.contentopsagent.agent.ToolCallTrace;
import com.contentopsagent.content.ContentQueryService;
import com.contentopsagent.content.model.ContentRecord;
import com.contentopsagent.content.model.ContentSearchCriteria;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class ContentSearchTool {

    public static final String NAME = "search_contents";

    private final ContentQueryService contentQueryService;
    private final ObjectMapper objectMapper;
    private final ToolCallTrace toolCallTrace;

    public ContentSearchTool(
            ContentQueryService contentQueryService,
            ObjectMapper objectMapper,
            ToolCallTrace toolCallTrace
    ) {
        this.contentQueryService = contentQueryService;
        this.objectMapper = objectMapper;
        this.toolCallTrace = toolCallTrace;
    }

    @Tool(name = NAME, description = "장르, 상태, 공개일 등 조건으로 콘텐츠를 검색한다. 자유 SQL을 실행하지 않는다.")
    public String searchContents(
            @ToolParam(description = "장르", required = false) String genre,
            @ToolParam(description = "연령 등급", required = false) String ageRating,
            @ToolParam(description = "콘텐츠 상태", required = false) String status,
            @ToolParam(description = "공개일 시작 YYYY-MM-DD", required = false) String releaseDateFrom,
            @ToolParam(description = "공개일 끝 YYYY-MM-DD", required = false) String releaseDateTo,
            @ToolParam(description = "연월 YYYY-MM. 있으면 해당 달로 공개일 범위를 대체한다", required = false) String yearMonth,
            @ToolParam(description = "서비스 지역", required = false) String serviceRegion,
            @ToolParam(description = "메타데이터 상태", required = false) String metadataStatus
    ) {
        ContentSearchCriteria criteria = ContentSearchCriteria.of(
                genre,
                ageRating,
                status,
                releaseDateFrom,
                releaseDateTo,
                yearMonth,
                serviceRegion,
                metadataStatus
        );
        List<ContentRecord> found = contentQueryService.search(criteria);
        try {
            String output = objectMapper.writeValueAsString(found);
            toolCallTrace.record(NAME, String.valueOf(criteria), output);
            return output;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("콘텐츠 검색 결과를 직렬화하지 못했습니다.", e);
        }
    }
}
