package com.example.movies_recommendation_engine.models;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Table(name = "cinema_building")
public class CinemaBuilding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String location;
    @OneToMany(mappedBy = "cinemaBuilding", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Screens>screens = new ArrayList<>();
    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    LocalDateTime updatedAt;

}
