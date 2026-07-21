package com.example.movies_recommendation_engine.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;


@RestController
@RequestMapping("/ai")
public class RecommendationController {
    private final MovieTools movieTools;
    private final ChatClient chatClient;
    public RecommendationController(MovieTools movieTools, @Qualifier("recommendationClient") ChatClient chatClient){
        this.movieTools=movieTools;
        this.chatClient=chatClient;
    }
    @GetMapping("/chat")
    public String chat(@RequestParam String query, @RequestParam(defaultValue = "default") String conversationId){
       return chatClient.prompt()
               .user(query)
               .tools(movieTools)
               .advisors(
                       a->a.param(ChatMemory.CONVERSATION_ID, conversationId)
               )
               .call()
               .content();
    }
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String query, @RequestParam(defaultValue = "default") String conversationId){
        return chatClient.prompt()
                .user(query)
                .tools(movieTools)
                .advisors(
                        a->a.param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .stream()
                .content();
    }
}
