package com.example.movies_recommendation_engine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class UserLoginRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Min(8)
    private String password;

}
