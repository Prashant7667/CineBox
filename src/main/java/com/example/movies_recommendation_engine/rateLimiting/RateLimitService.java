package com.example.movies_recommendation_engine.rateLimiting;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {
    private final StringRedisTemplate stringRedisTemplate;
    public RateLimitService(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate=stringRedisTemplate;
    }
    public boolean isAllowed(String key, int limit, int limit_windows ){
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if(count!=null && count ==1){
            stringRedisTemplate.expire(key, Duration.ofSeconds(limit_windows));
        }
        return count !=null && count<=limit;
    }
}
