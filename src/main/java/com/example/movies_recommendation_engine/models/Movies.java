package com.example.movies_recommendation_engine.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "movies",
        uniqueConstraints = {
            @UniqueConstraint(
                    name="ml_name_language",
                    columnNames = {"name", "language"}
            )
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Movies {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    Long duration;
    String language;
    String genre;
    String description;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    @Column(nullable = false)
    LocalDateTime updatedAt;

}
