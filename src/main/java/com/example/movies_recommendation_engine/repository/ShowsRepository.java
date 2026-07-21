package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.Shows;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowsRepository extends JpaRepository<Shows, Long> {
    @Query(
            """
                    SELECT s FROM Shows s JOIN FETCH s.screen sc JOIN FETCH sc.cinemaBuilding WHERE s.movies.id = :movieId AND s.startTime > CURRENT_TIMESTAMP""")
    List<Shows> findUpcomingByMovieId(@Param("movieId") Long movieId);
}
