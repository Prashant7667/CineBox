package com.example.movies_recommendation_engine.service;

import com.example.movies_recommendation_engine.dto.ScreensRequest;
import com.example.movies_recommendation_engine.dto.ScreensResponse;
import com.example.movies_recommendation_engine.exception.NotFoundException;
import com.example.movies_recommendation_engine.models.CinemaBuilding;
import com.example.movies_recommendation_engine.models.Screens;
import com.example.movies_recommendation_engine.repository.BuildingRepository;
import com.example.movies_recommendation_engine.repository.ScreensRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScreensService {
    private final ScreensRepository screensRepository;
    private final BuildingRepository buildingRepository;
    public ScreensService(ScreensRepository screensRepository, BuildingRepository buildingRepository){
        this.screensRepository=screensRepository;
        this.buildingRepository=buildingRepository;
    }
    public ScreensResponse convertScreensToScreenReponse(Screens screens){
        ScreensResponse screensResponse = new ScreensResponse();
        screensResponse.setId(screens.getId());
        screensResponse.setName(screens.getName());
        screensResponse.setBuildingId(screens.getCinemaBuilding().getId());
        screensResponse.setBuildingName(screens.getCinemaBuilding().getName());
        screensResponse.setSeatNames(new ArrayList<>());
        return screensResponse;

    }
    public ScreensResponse addScreens(Long buildingId, ScreensRequest screensRequest){
        CinemaBuilding cinemaBuilding = buildingRepository.findById(buildingId).orElseThrow(()-> new NotFoundException("Building not found with this id: " + buildingId));
        Screens screens = new Screens();
        screens.setName(screensRequest.getName());
        screens.setCinemaBuilding(cinemaBuilding);
        screensRepository.save(screens);

        return convertScreensToScreenReponse(screens);
    }
    public List<ScreensResponse> getScreensByBuildingId(Long buildingId){
        List<Screens> screens = screensRepository.findAllByCinemaBuildingId(buildingId);
        List<ScreensResponse>screensResponses = new ArrayList<>();
        for(Screens screen:screens){
            screensResponses.add(convertScreensToScreenReponse(screen));
        }
        return screensResponses;

    }

}
