package com.example.movies_recommendation_engine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BuildingRequest {
    @NotBlank
    String name;
    @NotBlank
    String location;
}
