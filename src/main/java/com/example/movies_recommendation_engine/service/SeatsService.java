package com.example.movies_recommendation_engine.service;

import com.example.movies_recommendation_engine.dto.SeatsRequest;
import com.example.movies_recommendation_engine.dto.SeatsResponse;
import com.example.movies_recommendation_engine.exception.NotFoundException;
import com.example.movies_recommendation_engine.models.Screens;
import com.example.movies_recommendation_engine.models.Seats;
import com.example.movies_recommendation_engine.repository.ScreensRepository;
import com.example.movies_recommendation_engine.repository.SeatsRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SeatsService {
    private final SeatsRepository seatsRepository;
    private final ScreensRepository screensRepository;
    public SeatsService(SeatsRepository seatsRepository, ScreensRepository screensRepository){
        this.seatsRepository = seatsRepository;
        this.screensRepository = screensRepository;
    }
    public SeatsResponse addSeats(Long screenId, List<SeatsRequest> seatsRequests){
        Screens screens = screensRepository.findById(screenId).orElseThrow(()->new NotFoundException("Screen not found with this id: " + screenId));
        List<String> seatNames = new ArrayList<>();
        List<Pair<String, Long>>seatDetails = new ArrayList<>();
        for(SeatsRequest seatsRequest:seatsRequests){
            Seats seats = new Seats();
            seats.setName(seatsRequest.getName());
            seats.setScreens(screens);
            seatsRepository.save(seats);
            seatNames.add(seats.getName());
            seatDetails.add(Pair.of(seats.getName(), seats.getId()));
        }
        SeatsResponse seatsResponse = new SeatsResponse();
        seatsResponse.setScreenId(screens.getId());
        seatsResponse.setScreenName(screens.getName());
        seatsResponse.setSeatDetails(seatDetails);
        return seatsResponse;
    }
}
