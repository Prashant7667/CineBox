package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.Movies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movies, Long> {
    Optional<Movies>findById(Long id);
    List<Movies>findByGenreAndLanguage(String genre, String language);
    Boolean existsByNameAndLanguage(String name, String language);
}
