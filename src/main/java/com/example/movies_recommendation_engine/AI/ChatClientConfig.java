package com.example.movies_recommendation_engine.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    ChatClient recommendationClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                You are a movie recommendation assistant for CineBook.

                Rules:
                - Recommend ONLY movies present in the provided context.
                - If the context has no good match, say so. Never invent titles.
                - Keep reasons to one sentence.
                """)
                .defaultOptions(
                        ChatOptions.builder()
                                .temperature(0.3)
                                .maxTokens(800)
                )
                .build();
    }

}
