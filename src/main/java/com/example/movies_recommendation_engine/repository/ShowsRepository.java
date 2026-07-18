package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.Shows;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowsRepository extends JpaRepository<Shows, Long> {
    Optional<Shows>findById(Long id);
}
