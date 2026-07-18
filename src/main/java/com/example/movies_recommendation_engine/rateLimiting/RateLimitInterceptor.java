package com.example.movies_recommendation_engine.rateLimiting;

import com.example.movies_recommendation_engine.exception.TooManyRequests;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitService rateLimitService;
    public RateLimitInterceptor(RateLimitService rateLimitService){
        this.rateLimitService=rateLimitService;
    }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if(!(handler instanceof HandlerMethod handlerMethod))return true;
        RateLimitAnnotation rateLimited = handlerMethod.getMethodAnnotation(RateLimitAnnotation.class);
        if(rateLimited==null)return true;
        String clientIp = request.getRemoteAddr();
        if(!rateLimitService.isAllowed(clientIp, rateLimited.limit(), rateLimited.durationSecond())){
            throw new TooManyRequests("Too many requests with this ip");
        }
        return true;
    }

}
