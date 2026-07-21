package com.example.movies_recommendation_engine.AI;

import com.example.movies_recommendation_engine.dto.MovieResponse;
import com.example.movies_recommendation_engine.dto.SeatsResponse;
import com.example.movies_recommendation_engine.dto.ShowTimingInfo;
import com.example.movies_recommendation_engine.exception.NotFoundException;
import com.example.movies_recommendation_engine.models.Movies;
import com.example.movies_recommendation_engine.models.Shows;
import com.example.movies_recommendation_engine.repository.MovieRepository;
import com.example.movies_recommendation_engine.repository.ShowsRepository;
import com.example.movies_recommendation_engine.service.BookingService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MovieTools {
    private final MovieRepository movieRepository;
    private final ShowsRepository showsRepository;
    private final VectorStore vectorStore;
    private final BookingService bookingService;
    public MovieTools(MovieRepository movieRepository, ShowsRepository showsRepository, VectorStore vectorStore, BookingService bookingService){
        this.movieRepository=movieRepository;
        this.showsRepository=showsRepository;
        this.vectorStore=vectorStore;
        this.bookingService=bookingService;
    }

    public record MovieSummary(Long id, String name, Long duration, String language, String genre) {}

    @Tool(description = "Search the movie catalog by genre and language. "
            + "Returns at most 10 currently-screening movies.")
    public List<MovieSummary> searchMovies(
            @ToolParam(description = "e.g. ACTION, COMEDY, HORROR") String genre,
            @ToolParam(description = "ISO code, e.g. hi, en, ta") String language) {
        return movieRepository.findByGenreAndLanguage(genre, language)
                .stream()
                .map(m -> new MovieSummary(m.getId(), m.getName(), m.getDuration(), m.getLanguage(), m.getGenre()))
                .toList();
    }

    @Tool(description = "Search the movie by name. ")
    public MovieResponse searchMovieByName(
            @ToolParam(description = "name with ignoring the case") String name){
        Movies movies = movieRepository.findByName(name).orElseThrow(()-> new NotFoundException("Movies Not found with this name : "+ name));
        return new MovieResponse(movies.getId(), movies.getName(), movies.getDuration(), movies.getLanguage(), movies.getGenre(), movies.getDescription());

    }

    @Tool(description = "Get upcoming show timing for a movie . Use after identifying which movie the user wants")
    public List<ShowTimingInfo>showTimingInfos(
            @ToolParam(description = "timing of the upcoming shows for the moving")Long movieId){
        List<Shows>shows = showsRepository.findUpcomingByMovieId(movieId);
        return shows.stream().map(show-> new ShowTimingInfo(show.getId(), show.getScreen().getName(), show.getScreen().getCinemaBuilding().getName(), show.getPrice(), show.getStartTime())).toList();
    }

    @Tool(description = "Semantic search for movies by description/mood/vibe. "
            + "Use when user describes what kind of movie they want rather than naming a genre.")
    public List<MovieSummary>discoverMovies(
            @ToolParam(description = "natural language description of what the User wants")String query){
        SearchRequest request = SearchRequest.builder().query(query).topK(5).build();
        List<Document> docs = vectorStore.similaritySearch(request);
        List<MovieSummary>movieSummaryList = new ArrayList<>();
        for(Document doc:docs){
            Long movieId = ((Number)doc.getMetadata().get("movieId")).longValue();
            Movies movie = movieRepository.findById(movieId).orElseThrow(()-> new NotFoundException("Movie not found with this id:"+ movieId));
            movieSummaryList.add(new MovieSummary(movie.getId(), movie.getName(), movie.getDuration(), movie.getLanguage(), movie.getGenre()));
        }
        return movieSummaryList;

    }

    @Tool(description = "Check how many seats are available for a specific show. Use after getShowTimings to tell the user seat availability.")
    public int getAvailableSeatCount(@ToolParam(description = "show ID from showTimingInfos result") Long showId) {
        SeatsResponse response = bookingService.availableSeats(showId);
        return response.getSeatDetails().size();
    }
}
