package com.example.movies_recommendation_engine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    @NotNull(message = "showId can't be empty")
    Long showId;
    @NotNull(message = "seatIds cant be empty here ")
    List<Long> seatIds;
}
