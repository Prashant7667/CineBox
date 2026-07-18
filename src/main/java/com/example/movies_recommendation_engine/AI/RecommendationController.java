package com.example.movies_recommendation_engine.AI;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/AI/")
public class RecommendationController {
    private final MovieTools movieTools;
    private final ChatClient chatClient;
    public RecommendationController(MovieTools movieTools, @Qualifier("recommendationClient") ChatClient chatClient){
        this.movieTools=movieTools;
        this.chatClient=chatClient;
    }
    @PostMapping("/MoviesByLangAndGenre")
    public String getRecommended(@RequestParam String query){
       return chatClient.prompt()
               .user(query)
               .tools(movieTools)
               .call()
               .content();
    }
}
