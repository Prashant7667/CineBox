package com.example.movies_recommendation_engine.rateLimiting;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class RateLimitConfigurer implements WebMvcConfigurer {
    private final RateLimitInterceptor rateLimitInterceptor;
    public RateLimitConfigurer(RateLimitInterceptor rateLimitInterceptor){
        this.rateLimitInterceptor=rateLimitInterceptor;
    }
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(rateLimitInterceptor);
    }
}
