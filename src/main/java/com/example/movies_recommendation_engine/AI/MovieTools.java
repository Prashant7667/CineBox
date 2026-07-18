package com.example.movies_recommendation_engine.AI;

import com.example.movies_recommendation_engine.models.Movies;
import com.example.movies_recommendation_engine.repository.MovieRepository;
import com.example.movies_recommendation_engine.repository.ShowsRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MovieTools {
    private final MovieRepository movieRepository;
    private final ShowsRepository showsRepository;
    public MovieTools(MovieRepository movieRepository, ShowsRepository showsRepository){
        this.movieRepository=movieRepository;
        this.showsRepository=showsRepository;
    }
    @Tool(description = "Search the movie catalog by genre and language. "
            + "Returns at most 10 currently-screening movies.")
    public List<Movies>searchMovies(
            @ToolParam(description = "e.g. ACTION, COMEDY, HORROR") String genre,
            @ToolParam(description = "ISO code, e.g. hi, en, ta") String language) {
        return movieRepository.findByGenreAndLanguage(genre, language);
    }

}
