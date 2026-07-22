package com.example.movies_recommendation_engine;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import static org.mockito.Mockito.mock;

@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(
        basePackages = "com.example.movies_recommendation_engine",
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.example\\.movies_recommendation_engine\\.AI\\..*"
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = MoviesRecommendationEngineApplication.class
                )
        }
)
public class TestConfig {

    @Bean
    VectorStore vectorStore() {
        return mock(VectorStore.class);
    }
}
