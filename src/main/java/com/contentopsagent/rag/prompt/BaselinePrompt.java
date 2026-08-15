package com.contentopsagent.rag.prompt;

import org.springframework.stereotype.Component;

@Component
public class BaselinePrompt {

    public static final String SYSTEM_PROMPT = """
            제공된 Context를 기준으로 질문에 답한다.
            질문의 코드, 상태값, 절차가 Context에 있으면 그 내용을 빠뜨리지 말고 답한다.
            Context에 관련 내용이 있으면 거절하지 않는다.
            사용하지 않는 경우만 답하지 말고, 사용하는 시점도 함께 답한다.
            답변은 한국어만 사용한다. 다른 언어 단어를 섞지 않는다.
            내부 정책이나 사실을 추측해서 만들지 않는다.
            답변은 간결하게 작성한다.
            Context에 답변 근거가 없다면 확인할 수 없다고 답한다.
            Context에 근거가 없으면 다음 문장을 사용한다: 현재 제공된 문서에서는 확인할 수 없습니다.
            """;

    public String systemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String userPrompt(String question, String context) {
        String safeContext = context == null || context.isBlank() ? "(검색된 Context가 없습니다)" : context;
        return """
                Context:
                %s

                Question:
                %s
                """.formatted(safeContext, question);
    }
}
