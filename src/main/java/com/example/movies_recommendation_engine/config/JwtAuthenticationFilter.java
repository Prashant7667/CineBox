package com.example.movies_recommendation_engine.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.movies_recommendation_engine.exception.AuthenticationException;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final UserDetailsImplService userDetailsImplService;
    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsImplService userDetailsImplService){
        this.jwtUtils=jwtUtils;
        this.userDetailsImplService=userDetailsImplService;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException{
        String authHeader = request.getHeader("Authorization");
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        try{
            String token = authHeader.substring(7);
            String email  = jwtUtils.getEmail(token);
            UserDetails userDetails = userDetailsImplService.loadUserByUsername(email);
            var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request,response);
        } catch (Exception e) {
            throw new AuthenticationException("User can't be authenticated because user name and password is incorrect");
        }


    }


}
