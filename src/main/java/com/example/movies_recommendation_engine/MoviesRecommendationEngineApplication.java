package com.example.movies_recommendation_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoviesRecommendationEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviesRecommendationEngineApplication.class, args);
	}

}
