package com.example.movies_recommendation_engine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatsResponse {
    Long screenId;
    String screenName;
    List<Pair<String, Long>> seatDetails;
}
