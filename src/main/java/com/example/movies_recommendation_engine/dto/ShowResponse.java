package com.example.movies_recommendation_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ShowResponse {
    Long id;
    String movieName;
    String screenName;
    String buildingName;
    double price;
    LocalDateTime startTime;
}
