package com.example.movies_recommendation_engine.service;
import com.example.movies_recommendation_engine.dto.UserSignupRequest;
import com.example.movies_recommendation_engine.dto.UserSignupResponse;
import com.example.movies_recommendation_engine.models.User;
import com.example.movies_recommendation_engine.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    public UserSignupResponse saveUser(UserSignupRequest userSignupRequest){
        User user = new User();
        user.setEmail(userSignupRequest.getEmail());
        user.setName(userSignupRequest.getName());
        user.setPhoneNumber(userSignupRequest.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(userSignupRequest.getPassword()));
        user.setRole(userSignupRequest.getRole());
        userRepository.save(user);
        return new UserSignupResponse(user.getId(), user.getEmail(), user.getName(), user.getPhoneNumber(), user.getRole());
    }
}
