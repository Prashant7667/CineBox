package com.example.movies_recommendation_engine.dto;

import com.example.movies_recommendation_engine.models.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserSignupRequest {
    @NotBlank
    @Email
    String email;
    @NotBlank
    String name;
    @NotBlank
    String phoneNumber;
    @NotBlank
    @Size(min = 8)
    String password;
    Role role;
}
