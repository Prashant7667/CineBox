package com.example.movies_recommendation_engine.service;

import com.example.movies_recommendation_engine.dto.BuildingRequest;
import com.example.movies_recommendation_engine.dto.BuildingResponse;
import com.example.movies_recommendation_engine.dto.ScreensResponse;
import com.example.movies_recommendation_engine.exception.NotFoundException;
import com.example.movies_recommendation_engine.models.CinemaBuilding;
import com.example.movies_recommendation_engine.models.Screens;
import com.example.movies_recommendation_engine.models.Seats;
import com.example.movies_recommendation_engine.repository.BuildingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BuildingService {
    private final BuildingRepository buildingRepository;
    public BuildingService(BuildingRepository buildingRepository){
        this.buildingRepository=buildingRepository;
    }
    public BuildingResponse addBuilding(BuildingRequest buildingRequest){
        CinemaBuilding cinemaBuilding = new CinemaBuilding();
        cinemaBuilding.setName(buildingRequest.getName());
        cinemaBuilding.setLocation(buildingRequest.getLocation());
        buildingRepository.save(cinemaBuilding);
        List<ScreensResponse>screensResponses = new ArrayList<>();
        for(Screens screen:cinemaBuilding.getScreens()){
            ScreensResponse screensResponse = new ScreensResponse();
            screensResponse.setId(screen.getId());
            screensResponse.setName(screen.getName());
            screensResponse.setBuildingId(cinemaBuilding.getId());
            screensResponse.setBuildingName(cinemaBuilding.getName());
            List<String>seatNames = new ArrayList<>();
            for(Seats seat:screen.getSeat()){
                seatNames.add(seat.getName());
            }
            screensResponse.setSeatNames(seatNames);
            screensResponses.add(screensResponse);
        }
        return new BuildingResponse(cinemaBuilding.getId(), cinemaBuilding.getName(), cinemaBuilding.getLocation(), screensResponses);
    }
    public CinemaBuilding addScreens(Screens screens){
        CinemaBuilding cinemaBuilding = buildingRepository.findById(screens.getCinemaBuilding().getId()).orElseThrow(()->new NotFoundException("No building is linked for this screen"));
        screens.setCinemaBuilding(cinemaBuilding);
        cinemaBuilding.getScreens().add(screens);
        return buildingRepository.save(cinemaBuilding);
    }
}
