package com.example.movies_recommendation_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class BookingResponse {
    Long id;
    String movieName;
    String screenName;
    String buildingName;
    String userName;
    double price;
    LocalDateTime showTime;
    List<String> seatNames;
}
