package com.example.movies_recommendation_engine.controllers;

import com.example.movies_recommendation_engine.dto.MovieRequest;
import com.example.movies_recommendation_engine.dto.MovieResponse;
import com.example.movies_recommendation_engine.dto.ShowRequest;
import com.example.movies_recommendation_engine.dto.ShowResponse;
import com.example.movies_recommendation_engine.models.Movies;
import com.example.movies_recommendation_engine.models.Seats;
import com.example.movies_recommendation_engine.models.Shows;
import com.example.movies_recommendation_engine.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;
    public MovieController(MovieService movieService){
        this.movieService=movieService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<MovieResponse> addMovies(@Valid @RequestBody MovieRequest movieAdd){
        MovieResponse movieResponse = movieService.addMovies(movieAdd);
        return ResponseEntity.status(HttpStatus.CREATED).body(movieResponse);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping ("/remove/{movieId}")
    public ResponseEntity<Void> removeMovies(@PathVariable Long movieId){
        movieService.removeMovies(movieId);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/shows/add")
    public ResponseEntity<ShowResponse> addShows(@Valid @RequestBody ShowRequest show){
        ShowResponse addedShow = movieService.addShows(show);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedShow);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/shows/remove/{showId}")
    public ResponseEntity<Void> removeShows(@PathVariable Long showId){
        movieService.removeShows(showId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<MovieResponse>>getAllMovies(){
        return ResponseEntity.ok(movieService.getAllMovies());
    }
    @GetMapping("{id}")
    public ResponseEntity<MovieResponse>getMovieById(@PathVariable Long id){
        return ResponseEntity.ok(movieService.getMovieById(id));
    }


}
