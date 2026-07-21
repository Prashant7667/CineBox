package com.example.movies_recommendation_engine.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "booked_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"show_id", "seat_id"}
                )
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookedSeats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne
            @JoinColumn(name = "booking_id")
    Booking booking;
    @ManyToOne
            @JoinColumn(name = "show_id")
    Shows show;
    @ManyToOne
            @JoinColumn(name = "seat_id")
    Seats seat;



}
