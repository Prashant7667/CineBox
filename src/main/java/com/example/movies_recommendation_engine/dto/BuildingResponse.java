package com.example.movies_recommendation_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BuildingResponse {
    Long id;
    String name;
    String location;
    List<ScreensResponse> screens;
}
