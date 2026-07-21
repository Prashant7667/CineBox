package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.BookedSeats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookedSeatsRepository extends JpaRepository<BookedSeats, Long> {
    BookedSeats findByShowIdAndSeatId(Long showId, Long seatId);
}
