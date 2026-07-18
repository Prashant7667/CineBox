package com.example.movies_recommendation_engine.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MovieRequest {
    @NotBlank
    String name;
    @Min(1)
    Long duration;
    @NotBlank
    String language;
    @NotBlank
    String genre;
}
