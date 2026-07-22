package com.example.movies_recommendation_engine;

import com.example.movies_recommendation_engine.dto.*;
import com.example.movies_recommendation_engine.models.*;
import com.example.movies_recommendation_engine.repository.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("test_movie_engine")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired TestRestTemplate restTemplate;

    static String userToken;
    static Long showId;
    static Long seat1Id;
    static Long seat2Id;

    @BeforeAll
    static void seedData(
            @Autowired UserRepository userRepository,
            @Autowired MovieRepository movieRepository,
            @Autowired BuildingRepository buildingRepository,
            @Autowired ScreensRepository screensRepository,
            @Autowired SeatsRepository seatsRepository,
            @Autowired ShowsRepository showsRepository,
            @Autowired PasswordEncoder passwordEncoder,
            @Autowired TestRestTemplate restTemplate
    ) {
        User user = new User();
        user.setEmail("booker@example.com");
        user.setName("Booker");
        user.setPhoneNumber("1111111111");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        Movies movie = new Movies();
        movie.setName("Test Movie");
        movie.setDuration(120L);
        movie.setLanguage("English");
        movie.setGenre("Action");
        movie.setDescription("A test movie");
        movieRepository.save(movie);

        CinemaBuilding building = new CinemaBuilding();
        building.setName("Test Cinema");
        building.setLocation("Test City");
        buildingRepository.save(building);

        Screens screen = new Screens();
        screen.setName("Screen 1");
        screen.setCinemaBuilding(building);
        screensRepository.save(screen);

        Seats s1 = new Seats();
        s1.setName("A1");
        s1.setScreens(screen);
        s1 = seatsRepository.save(s1);
        seat1Id = s1.getId();

        Seats s2 = new Seats();
        s2.setName("A2");
        s2.setScreens(screen);
        s2 = seatsRepository.save(s2);
        seat2Id = s2.getId();

        Shows show = new Shows();
        show.setMovies(movie);
        show.setScreen(screen);
        show.setPrice(250.0);
        show.setStartTime(LocalDateTime.now().plusDays(1));
        show = showsRepository.save(show);
        showId = show.getId();

        UserLoginRequest login = new UserLoginRequest("booker@example.com", "password123");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/users/login", login, Map.class);
        userToken = (String) loginResponse.getBody().get("token");
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    @Order(1)
    void availableSeats_shouldReturnAllSeats() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        // Use String to avoid Jackson deserialization issues with Pair<String, Long>
        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/shows/" + showId + "/seats",
                HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verify both seat names appear in the JSON response
        assertTrue(response.getBody().contains("A1"));
        assertTrue(response.getBody().contains("A2"));
    }

    @Test
    @Order(2)
    void seatReservation_shouldReserveSeats() {
        List<Long> seatIds = List.of(seat1Id);
        HttpEntity<List<Long>> entity = new HttpEntity<>(seatIds, authHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/seatSelection/" + showId,
                HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("A1"));
    }

    @Test
    @Order(3)
    void addBooking_shouldBookReservedSeats() {
        BookingRequest request = new BookingRequest();
        request.setShowId(showId);
        request.setSeatIds(List.of(seat1Id));
        HttpEntity<BookingRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/ticket", HttpMethod.POST, entity, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Test Movie"));
        assertTrue(response.getBody().contains("Screen 1"));
    }

    @Test
    @Order(4)
    void availableSeats_afterBooking_shouldExcludeBookedSeat() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/shows/" + showId + "/seats",
                HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // A1 is booked, only A2 should be available
        assertFalse(response.getBody().contains("A1"));
        assertTrue(response.getBody().contains("A2"));
    }

    @Test
    @Order(5)
    void doubleBooking_sameSeat_shouldBeRejected() {
        List<Long> seatIds = List.of(seat1Id);
        HttpEntity<List<Long>> reserveEntity = new HttpEntity<>(seatIds, authHeaders());

        ResponseEntity<String> reserveResponse = restTemplate.exchange(
                "/bookings/seatSelection/" + showId,
                HttpMethod.POST, reserveEntity, String.class);

        // Seat A1 is already booked — should be rejected
        assertEquals(HttpStatus.CONFLICT, reserveResponse.getStatusCode());
    }

    @Test
    @Order(6)
    void getMyBookings_shouldReturnBookedTickets() {
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/me", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Test Movie"));
    }
}
