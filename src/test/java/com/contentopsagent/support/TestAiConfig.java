package com.contentopsagent.support;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestAiConfig {

    @Bean
    @Primary
    EmbeddingModel embeddingModel() {
        return new DeterministicEmbeddingModel(32);
    }

    @Bean
    @Primary
    ChatModel chatModel() {
        return new StubChatModel("테스트 답변입니다.");
    }

    @Bean
    @Primary
    ChatClient.Builder chatClientBuilder(ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }
}
