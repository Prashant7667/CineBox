package com.example.movies_recommendation_engine.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.rmi.AccessException;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandling {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError>notFoundException(NotFoundException ex){
        ApiError apiError = new ApiError(404, "NotFound", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiError);

    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError>authenticationException(AuthenticationException ex){
        ApiError apiError = new ApiError(401, "Unauthorized", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }
    @ExceptionHandler(TooManyRequests.class)
    public ResponseEntity<ApiError>tooManyRequestException(TooManyRequests ex){
        ApiError apiError = new ApiError(429, "Too many request", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(apiError);
    }
    @ExceptionHandler(SeatBookedException.class)
    public ResponseEntity<ApiError>SeatBookedException(SeatBookedException ex){
        ApiError apiError = new ApiError(409, "Seat is Not available for booking", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiError>DuplicateException (DuplicateException ex){
        ApiError apiError = new ApiError(409, "Duplicate Entry ", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(apiError);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError>exception(Exception ex){
        ApiError apiError = new ApiError(500, "Uncovered Exception", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
    @ExceptionHandler(AccessException.class)
    public ResponseEntity<ApiError>accessDeniedException(Exception ex){
        ApiError apiError = new ApiError(403, "Access Not Granted", ex.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }

}
