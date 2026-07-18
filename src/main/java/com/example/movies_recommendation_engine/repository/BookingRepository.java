package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.Booking;
import com.example.movies_recommendation_engine.models.Seats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(
            """
                    SELECT B FROM Booking B JOIN B.user AS U WHERE U.email = :email
                    """
    )
    List<Booking>findAllByEmail(@Param("email")String email);
    @Query(
            """
                   SELECT s FROM Booking b JOIN b.bookingSeats s WHERE b.show.id = :showId """
    )
    List<Seats> bookedSeatsByShowId(@Param("showId") Long showId);
}
