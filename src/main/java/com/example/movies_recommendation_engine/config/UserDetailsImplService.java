package com.example.movies_recommendation_engine.config;

import com.example.movies_recommendation_engine.exception.NotFoundException;
import com.example.movies_recommendation_engine.models.User;
import com.example.movies_recommendation_engine.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsImplService implements UserDetailsService {
    private final UserRepository userRepository;
    public UserDetailsImplService(UserRepository userRepository){
        this.userRepository=userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new NotFoundException("User Does not exist with this email"));
        return new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getRole().toString());
    }
}
