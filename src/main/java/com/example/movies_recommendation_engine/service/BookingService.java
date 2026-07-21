package com.example.movies_recommendation_engine.service;

import com.example.movies_recommendation_engine.dto.BookingRequest;
import com.example.movies_recommendation_engine.dto.BookingResponse;
import com.example.movies_recommendation_engine.dto.SeatsResponse;
import com.example.movies_recommendation_engine.exception.NotFoundException;
import com.example.movies_recommendation_engine.exception.SeatBookedException;
import com.example.movies_recommendation_engine.models.BookedSeats;
import com.example.movies_recommendation_engine.models.*;
import com.example.movies_recommendation_engine.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ShowsRepository showsRepository;
    private final UserRepository userRepository;
    private final SeatsRepository seatsRepository;
    private final ScreensRepository screensRepository;
    private final BookedSeatsRepository bookedSeatsRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public BookingService(BookingRepository bookingRepository, ShowsRepository showsRepository, UserRepository userRepository, SeatsRepository seatsRepository, StringRedisTemplate stringRedisTemplate, ScreensRepository screensRepository, BookedSeatsRepository bookedSeatsRepository){
        this.bookingRepository=bookingRepository;
        this.showsRepository=showsRepository;
        this.userRepository=userRepository;
        this.seatsRepository=seatsRepository;
        this.stringRedisTemplate=stringRedisTemplate;
        this.screensRepository=screensRepository;
        this.bookedSeatsRepository=bookedSeatsRepository;
    }
    public void removeSeatsFromRedis(List<String>keysList){
        for(String key:keysList){
            stringRedisTemplate.delete(key);
        }
    }
    public SeatsResponse seatReservation(List<Long>seats, Long showId){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        SeatsResponse seatsReserved= new SeatsResponse();
        Shows show =showsRepository.findById(showId).orElseThrow(()->new NotFoundException("Show not found with this showID:"+ showId));
        seatsReserved.setScreenId(show.getScreen().getId());
        seatsReserved.setScreenName(show.getScreen().getName());
        List<Pair<String, Long>>seatDetails = new ArrayList<>();
        List<String>keysAddedToRedis = new ArrayList<>();
        for(Long id:seats){
            Seats seat = seatsRepository.findById(id).orElseThrow(()-> new NotFoundException("seat with this id does not exist in the database"));
            BookedSeats bookedSeats = bookedSeatsRepository.findByShowIdAndSeatId(showId, id);
            if(bookedSeats!=null){
                removeSeatsFromRedis(keysAddedToRedis);
                throw  new SeatBookedException("For this show :"+ showId + "this seat :"+id+"is already booked");
            }
            String key = showId.toString()+"_" + id;
            Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(key, email, Duration.ofMinutes(10));
            if(Boolean.FALSE.equals(locked)){
                String holder = stringRedisTemplate.opsForValue().get(key);
                if(!email.equals(holder)){
                    removeSeatsFromRedis(keysAddedToRedis);
                    throw new SeatBookedException("Seat with this id : "+ id + "already reserved by a diff customer");
                }

            }
            keysAddedToRedis.add(key);
            seatDetails.add(Pair.of(seat.getName(),id));
        }
        seatsReserved.setSeatDetails(seatDetails);
        return seatsReserved;
    }
    @Transactional
    public BookingResponse addBooking(BookingRequest bookingRequest){
        Booking booking = new Booking();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Shows show = showsRepository.findById(bookingRequest.getShowId()).orElseThrow(() -> new NotFoundException("Show not found with this id: " + bookingRequest.getShowId()));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found with this email "));
        List<Seats> requestedSeats = seatsRepository.findAllById(bookingRequest.getSeatIds());
        List<BookedSeats>bookedSeats = new ArrayList<>();
        List<Pair<String, Long>>bookedSeatsDetails = new ArrayList<>();
        List<String>keysAddedToRedis = new ArrayList<>();
        if(requestedSeats.size() != bookingRequest.getSeatIds().size()){
            throw new NotFoundException("One or more seats not found");
        }
        for(Seats seat : requestedSeats){
            String key = show.getId().toString()+"_" + seat.getId();
            String existingEmailWithThisKey = stringRedisTemplate.opsForValue().get(key);
            if(existingEmailWithThisKey==null || existingEmailWithThisKey.isEmpty()||!(user.getEmail().equals(existingEmailWithThisKey))){
                throw new SeatBookedException("Seat with this id : "+ seat.getId() + "already reserved by a diff customer");
            }
            keysAddedToRedis.add(key);
            bookedSeatsDetails.add(Pair.of(seat.getName(),seat.getId()));
            BookedSeats bookedSeat = new BookedSeats();
            bookedSeat.setBooking(booking);
            bookedSeat.setSeat(seat);
            bookedSeat.setShow(show);
            bookedSeats.add(bookedSeat);
        }
        booking.setShow(show);
        booking.setUser(user);
        booking.setBookedSeats(bookedSeats);
        try{
            bookingRepository.saveAndFlush(booking);
            removeSeatsFromRedis(keysAddedToRedis);
        }
        catch (DataIntegrityViolationException exception){
            removeSeatsFromRedis(keysAddedToRedis);
            throw new SeatBookedException(exception.getMessage());
        }
        return new BookingResponse(booking.getId(),show.getMovies().getName(),show.getScreen().getName(),show.getScreen().getCinemaBuilding().getName(), user.getName(), show.getPrice(), show.getStartTime(),bookedSeatsDetails );
    }
    public SeatsResponse availableSeats(Long showId){
        List<BookedSeats>bookedSeats = bookingRepository.bookedSeatsByShowId(showId);
        Shows shows = showsRepository.findById(showId).orElseThrow(()-> new NotFoundException("show not found with this showId :"+ showId));
        List<Seats>totalSeats = shows.getScreen().getSeat();
        Set<Long> bookedSeatsId = bookedSeats.stream().map(bookedSeat->bookedSeat.getSeat().getId()).collect(Collectors.toSet());
        List<Seats>availableSeats = totalSeats.stream().filter(seats -> !(bookedSeatsId.contains(seats.getId()))).toList();
        SeatsResponse seatsResponses = new SeatsResponse();
        seatsResponses.setScreenId(shows.getScreen().getId());
        seatsResponses.setScreenName(shows.getScreen().getName());
        List<Pair<String, Long>>seatNames = new ArrayList<>();
        for(Seats seats:availableSeats){
            seatNames.add(Pair.of(seats.getName(), seats.getId()));
        }
        seatsResponses.setSeatDetails(seatNames);
        return seatsResponses;
    }
    public List<BookingResponse> getMyBooking(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Booking>bookings = bookingRepository.findAllByEmail(email);
        List<BookingResponse>bookingResponses = new ArrayList<>();
        for(Booking booking:bookings){
            bookingResponses.add(new BookingResponse(booking.getId(), booking.getShow().getMovies().getName(), booking.getShow().getScreen().getName(), booking.getShow().getScreen().getCinemaBuilding().getName(), booking.getUser().getName(), booking.getShow().getPrice(), booking.getShow().getStartTime(),booking.getBookedSeats().stream().map(bookedSeat->Pair.of(bookedSeat.getSeat().getName(), bookedSeat.getSeat().getId())).toList()));
        }
        return bookingResponses;

    }
}
