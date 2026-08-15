package com.contentopsagent.agent;

import com.contentopsagent.common.exception.AppException;
import com.contentopsagent.common.exception.ErrorCode;
import com.contentopsagent.question.dto.SourceDto;
import com.contentopsagent.tool.ContentDetailTool;
import com.contentopsagent.tool.ContentSearchTool;
import com.contentopsagent.tool.PolicySearchTool;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);
    private static final Pattern SOURCE_PATTERN = Pattern.compile("Document:\\s*(.+)\\nSection:\\s*(.+)");
    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");

    private final ChatClient chatClient;
    private final AgentPrompt agentPrompt;
    private final ToolCallTrace toolCallTrace;

    public AgentService(
            ChatClient.Builder chatClientBuilder,
            AgentPrompt agentPrompt,
            ToolCallTrace toolCallTrace,
            PolicySearchTool policySearchTool,
            ContentSearchTool contentSearchTool,
            ContentDetailTool contentDetailTool
    ) {
        this.chatClient = chatClientBuilder
                .defaultTools(policySearchTool, contentSearchTool, contentDetailTool)
                .build();
        this.agentPrompt = agentPrompt;
        this.toolCallTrace = toolCallTrace;
    }

    public AgentAnswer ask(String question) {
        toolCallTrace.begin();
        long started = System.nanoTime();
        try {
            String answer = chatClient.prompt()
                    .system(agentPrompt.systemPrompt(YearMonth.now(ZONE)))
                    .user(question)
                    .call()
                    .content();
            long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
            List<RecordedToolCall> calls = toolCallTrace.snapshot();
            if (answer == null || answer.isBlank()) {
                throw new AppException(ErrorCode.LLM_GENERATION_FAILED);
            }
            log.info(
                    "agent questionLength={} tools={} latencyMs={}",
                    question.length(),
                    calls.stream().map(RecordedToolCall::name).toList(),
                    latencyMs
            );
            return new AgentAnswer(answer.trim(), calls, sourcesFrom(calls), latencyMs);
        } catch (AppException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.LLM_GENERATION_FAILED, e);
        } finally {
            toolCallTrace.clear();
        }
    }

    private List<SourceDto> sourcesFrom(List<RecordedToolCall> calls) {
        Set<String> seen = new LinkedHashSet<>();
        List<SourceDto> sources = new ArrayList<>();
        for (RecordedToolCall call : calls) {
            if (!PolicySearchTool.NAME.equals(call.name())) {
                continue;
            }
            Matcher matcher = SOURCE_PATTERN.matcher(call.result());
            while (matcher.find()) {
                String document = matcher.group(1).trim();
                String section = matcher.group(2).trim();
                String key = document + "|" + section;
                if (seen.add(key)) {
                    sources.add(new SourceDto(document, section));
                }
            }
        }
        return sources;
    }
}
