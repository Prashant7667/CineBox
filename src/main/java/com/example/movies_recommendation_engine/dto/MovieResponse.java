package com.example.movies_recommendation_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MovieResponse {
    Long id;
    String name;
    Long duration;
    String language;
    String genre;
    String description;
}
