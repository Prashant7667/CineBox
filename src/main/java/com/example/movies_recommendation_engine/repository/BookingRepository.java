package com.example.movies_recommendation_engine.repository;

import com.example.movies_recommendation_engine.models.BookedSeats;
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
    SELECT b FROM Booking b JOIN FETCH b.show s JOIN FETCH s.movies JOIN FETCH s.screen sc JOIN FETCH sc.cinemaBuilding JOIN FETCH b.user JOIN FETCH b.bookedSeats bs JOIN FETCH bs.seat WHERE 
    b.user.email = :email""")
    List<Booking>findAllByEmail(@Param("email")String email);

    @Query(
            """
                   SELECT s FROM Booking b JOIN b.bookedSeats s WHERE b.show.id = :showId """
    )
    List<BookedSeats> bookedSeatsByShowId(@Param("showId") Long showId);
}
