package com.contentopsagent.agent;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ToolCallTrace {

    private static final ThreadLocal<List<RecordedToolCall>> CALLS = ThreadLocal.withInitial(ArrayList::new);

    public void begin() {
        CALLS.get().clear();
    }

    public void record(String name, String input, String result) {
        CALLS.get().add(new RecordedToolCall(
                name,
                input == null ? "" : input,
                result == null ? "" : result
        ));
    }

    public List<RecordedToolCall> snapshot() {
        return List.copyOf(CALLS.get());
    }

    public void clear() {
        CALLS.remove();
    }
}
