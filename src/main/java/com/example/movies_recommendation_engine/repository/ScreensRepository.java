package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.Screens;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScreensRepository extends JpaRepository<Screens, Long> {
    Optional<Screens>findById(Long id);
    List<Screens> findAllByCinemaBuildingId(Long buildingId);

}
