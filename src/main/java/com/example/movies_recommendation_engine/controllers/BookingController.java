package com.example.movies_recommendation_engine.controllers;

import com.example.movies_recommendation_engine.dto.BookingRequest;
import com.example.movies_recommendation_engine.dto.BookingResponse;
import com.example.movies_recommendation_engine.dto.SeatsResponse;
import com.example.movies_recommendation_engine.models.Seats;
import com.example.movies_recommendation_engine.rateLimiting.RateLimitAnnotation;
import com.example.movies_recommendation_engine.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookingService;
    public BookingController(BookingService bookingService){
        this.bookingService=bookingService;
    }
    @PostMapping("ticket")
    @RateLimitAnnotation(limit = 10, durationSecond = 6000)
    public ResponseEntity<BookingResponse> addBooking(@Valid @RequestBody BookingRequest bookingRequest){
        BookingResponse ticket = bookingService.addBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }
    @PostMapping("seatSelection/{showId}")
    public ResponseEntity<SeatsResponse> seatReservation(@Valid @RequestBody List<Long> seats, @PathVariable Long showId){
        SeatsResponse seatsResponse = bookingService.seatReservation(seats, showId);
        return ResponseEntity.status(HttpStatus.OK).body(seatsResponse);
    }
    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<SeatsResponse> availableSeats(@PathVariable Long showId){
        return ResponseEntity.ok(bookingService.availableSeats(showId));
    }
    @GetMapping("me")
    public ResponseEntity<List<BookingResponse>> allBookingReponse(){
        return ResponseEntity.ok(bookingService.getMyBooking());
    }
}
