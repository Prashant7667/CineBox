package com.example.movies_recommendation_engine.controllers;

import com.example.movies_recommendation_engine.dto.*;
import com.example.movies_recommendation_engine.models.CinemaBuilding;
import com.example.movies_recommendation_engine.models.Screens;
import com.example.movies_recommendation_engine.models.Seats;
import com.example.movies_recommendation_engine.service.BuildingService;
import com.example.movies_recommendation_engine.service.ScreensService;
import com.example.movies_recommendation_engine.service.SeatsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/building/")
public class BuildingController {
    private final BuildingService buildingService;
    private final SeatsService seatsService;
    private final ScreensService screensService;
    public BuildingController(BuildingService buildingService, SeatsService seatsService, ScreensService screensService){
        this.buildingService=buildingService;
        this.seatsService=seatsService;
        this.screensService=screensService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<BuildingResponse> addBuilding(@Valid @RequestBody BuildingRequest buildingRequest){
        BuildingResponse building=buildingService.addBuilding(buildingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(building);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/{buildingId}/screen")
    public ResponseEntity<ScreensResponse>addScreens(@PathVariable Long buildingId, @Valid @RequestBody ScreensRequest screensAdd){
        ScreensResponse screens=screensService.addScreens(buildingId, screensAdd);
        return ResponseEntity.status(HttpStatus.CREATED).body(screens);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add/{screenId}/seats")
    public ResponseEntity<SeatsResponse>addSeat(@PathVariable Long screenId, @Valid @RequestBody List<SeatsRequest> seatsAdd){
        SeatsResponse seats=seatsService.addSeats(screenId, seatsAdd);
        return ResponseEntity.status(HttpStatus.CREATED).body(seats);
    }
    @GetMapping("/{buildingId}")
    public ResponseEntity<List<ScreensResponse>>getScreenByBuilding(@PathVariable Long buildingId){
        return ResponseEntity.ok(screensService.getScreensByBuildingId(buildingId));
    }
}

