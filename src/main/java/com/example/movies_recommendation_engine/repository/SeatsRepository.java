package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.Seats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatsRepository extends JpaRepository<Seats, Long> {
}
