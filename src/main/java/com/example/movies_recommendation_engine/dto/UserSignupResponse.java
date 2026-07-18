package com.example.movies_recommendation_engine.dto;

import com.example.movies_recommendation_engine.models.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSignupResponse {
    Long id;
    String email;
    String name;
    String phoneNumber;
    Role role;
}
