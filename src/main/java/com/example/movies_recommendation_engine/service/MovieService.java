package com.example.movies_recommendation_engine.service;

import com.example.movies_recommendation_engine.dto.*;
import com.example.movies_recommendation_engine.exception.DuplicateException;
import com.example.movies_recommendation_engine.exception.NotFoundException;
import com.example.movies_recommendation_engine.models.Movies;
import com.example.movies_recommendation_engine.models.Screens;
import com.example.movies_recommendation_engine.models.Shows;
import com.example.movies_recommendation_engine.repository.MovieRepository;
import com.example.movies_recommendation_engine.repository.ScreensRepository;
import com.example.movies_recommendation_engine.repository.ShowsRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MovieService {
    private final MovieRepository movieRepository;
    private final ShowsRepository showsRepository;
    private final ScreensRepository screensRepository;
    private final VectorStore vectorStore;
    public MovieService(MovieRepository movieRepository, ShowsRepository showsRepository, ScreensRepository screensRepository, VectorStore vectorStore){
        this.movieRepository=movieRepository;
        this.showsRepository=showsRepository;
        this.screensRepository=screensRepository;
        this.vectorStore=vectorStore;
    }
    public MovieResponse addMovies(MovieRequest movieRequest){
        if(movieRepository.existsByNameAndLanguage(movieRequest.getName(), movieRequest.getLanguage())){
            throw new DuplicateException("With this Name and language movie is already added");
        }
        Movies movie = new Movies();
        movie.setName(movieRequest.getName());
        movie.setLanguage(movieRequest.getLanguage());
        movie.setDuration(movieRequest.getDuration());
        movie.setGenre(movieRequest.getGenre());
        movie.setDescription(movieRequest.getDescription());
        movieRepository.save(movie);
        Document doc = new Document(
                "movie-"+movie.getId(),
                 movie.getDescription(),
                Map.of(
                        "movieId", movie.getId(),
                        "name", movie.getName(),
                        "genre", movie.getGenre(),
                        "language", movie.getLanguage()
                )

        );
        vectorStore.add(List.of(doc));
        return new MovieResponse(movie.getId(), movie.getName(), movie.getDuration(),movie.getLanguage(),movie.getGenre(), movie.getDescription());
    }
    public void removeMovies(Long movieId){
        Movies movie = movieRepository.findById(movieId).orElseThrow(()-> new NotFoundException("Movie not found with this id: " + movieId));
        movieRepository.delete(movie);
    }
    public List<MovieResponse>getAllMovies(){
        List<Movies>movies = movieRepository.findAll();
        List<MovieResponse>movieResponses = new ArrayList<>();
        for(Movies movie : movies){
            movieResponses.add(new MovieResponse(movie.getId(), movie.getName(), movie.getDuration(),movie.getLanguage(),movie.getGenre(), movie.getDescription()));
        }
        return movieResponses;
    }
    public MovieResponse getMovieById(Long id){
        Movies movie = movieRepository.findById(id).orElseThrow(()-> new NotFoundException("Movie not found with this id: " + id));
        return new MovieResponse(movie.getId(), movie.getName(), movie.getDuration(),movie.getLanguage(),movie.getGenre(), movie.getDescription());
    }
    public ShowResponse addShows(ShowRequest showRequest){

        Movies movie = movieRepository.findById(showRequest.getMovieId()).orElseThrow(()-> new NotFoundException("Movie not found with this id: " + showRequest.getMovieId()));
        Screens screen = screensRepository.findById(showRequest.getScreenId()).orElseThrow(()->new NotFoundException("Screen not found with this id: " + showRequest.getScreenId()));
        Shows show= new Shows();
        show.setPrice(showRequest.getPrice());
        show.setMovies(movie);
        show.setScreen(screen);
        show.setStartTime(showRequest.getStartTime());
        showsRepository.save(show);
        return new ShowResponse(show.getId(), show.getMovies().getName(), show.getScreen().getName(), show.getScreen().getCinemaBuilding().getName(), show.getPrice(), show.getStartTime());
    }
    public void removeShows(Long showId){
        Shows show = showsRepository.findById(showId).orElseThrow(()-> new NotFoundException("Show not found with this id: " + showId));
        showsRepository.delete(show);
    }

}
