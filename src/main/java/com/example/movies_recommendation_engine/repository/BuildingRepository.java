package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.CinemaBuilding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface BuildingRepository extends JpaRepository<CinemaBuilding, Long> {
    public Optional<CinemaBuilding>findById(Long id);
}
