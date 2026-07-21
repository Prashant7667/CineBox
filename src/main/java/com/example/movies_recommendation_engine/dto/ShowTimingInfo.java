package com.example.movies_recommendation_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class ShowTimingInfo {
    Long showId;
    String screenName;
    String buildingName;
    double price;
    LocalDateTime startTime;
}
