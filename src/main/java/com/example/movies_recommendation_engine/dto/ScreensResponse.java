package com.example.movies_recommendation_engine.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScreensResponse {
    Long id;
    String name;
    Long buildingId;
    String buildingName;
    List<String> seatNames;
}
