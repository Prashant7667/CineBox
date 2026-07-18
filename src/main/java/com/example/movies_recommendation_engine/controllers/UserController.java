package com.example.movies_recommendation_engine.controllers;
import com.example.movies_recommendation_engine.config.JwtUtils;
import com.example.movies_recommendation_engine.config.UserDetailsImpl;
import com.example.movies_recommendation_engine.dto.UserLoginRequest;
import com.example.movies_recommendation_engine.dto.UserSignupRequest;
import com.example.movies_recommendation_engine.dto.UserSignupResponse;
import com.example.movies_recommendation_engine.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/Users/")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtils jwtUtils){
        this.userService=userService;
        this.authenticationManager=authenticationManager;
        this.jwtUtils=jwtUtils;
    }
    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signup(@Valid @RequestBody UserSignupRequest userSignupRequest){
        UserSignupResponse userSignupResponse = userService.saveUser(userSignupRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userSignupResponse);
    }
    @PostMapping("/login")
    public ResponseEntity<?>login(@Valid @RequestBody UserLoginRequest userLoginRequest){
        var authToken = new UsernamePasswordAuthenticationToken(userLoginRequest.getEmail(),userLoginRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails.getUsername(), userDetails.getRole());
        return ResponseEntity.ok(Map.of("token",jwt,"role",userDetails.getRole()));
    }
}