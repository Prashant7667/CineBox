package com.example.movies_recommendation_engine.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

@Data
public class ShowRequest {
    @NotNull
    Long movieId;
    @NotNull
    Long screenId;
    @NotNull
    double price;
    @Future
    LocalDateTime startTime;
}
