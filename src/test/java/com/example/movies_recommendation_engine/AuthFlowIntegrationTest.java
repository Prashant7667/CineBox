package com.example.movies_recommendation_engine;

import com.example.movies_recommendation_engine.dto.UserLoginRequest;
import com.example.movies_recommendation_engine.dto.UserSignupRequest;
import com.example.movies_recommendation_engine.dto.UserSignupResponse;
import com.example.movies_recommendation_engine.models.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest(webEnvironment = RANDOM_PORT)
// Starts the FULL Spring Boot application on a random available port.
// This is NOT a mock — it's your real app running with real controllers,
// real security filters, real services, real JPA repositories.
@SpringBootTest(classes = TestConfig.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// @Testcontainers — tells JUnit to look for @Container fields and manage their lifecycle.
// Before tests: start the containers. After tests: stop and destroy them.
@Testcontainers
@AutoConfigureTestRestTemplate

// @ActiveProfiles("test") — loads application-test.yml instead of application-local.yml.
// This is how we exclude AI beans and set a test JWT secret.
@ActiveProfiles("test")

// @TestMethodOrder — run tests in the order specified by @Order annotations.
// Normally test order is random (good practice), but for an integration FLOW
// where signup must happen before login, we need ordering.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthFlowIntegrationTest {

    // @Container — Testcontainers starts this Docker container before tests.
    // PostgreSQLContainer is a pre-built helper that:
    //   1. Pulls the postgres Docker image
    //   2. Starts it with a random port
    //   3. Creates a database called "test"
    //   4. Provides getJdbcUrl(), getUsername(), getPassword() to connect
    // After all tests finish, the container is destroyed — your real DB is untouched.
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("pgvector/pgvector:pg16")
            .withDatabaseName("test_movie_engine")
            .withUsername("test")
            .withPassword("test");

    // GenericContainer is used when there's no specialized container class.
    // Redis is simple enough that we just specify the image and exposed port.
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    // @DynamicPropertySource — injects runtime values into Spring's config.
    // The container ports are random (to avoid conflicts), so we can't hardcode them
    // in application-test.yml. This method runs AFTER containers start but BEFORE
    // Spring Boot starts, so Spring gets the correct dynamic URLs.
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    // TestRestTemplate — Spring Boot injects this automatically when using RANDOM_PORT.
    // It's pre-configured to call http://localhost:<random-port>.
    // You use it just like RestTemplate: postForEntity, getForEntity, exchange, etc.
    @Autowired
    TestRestTemplate restTemplate;

    // Static field to store the JWT token across tests.
    // Since tests share the same app instance, the user we create in test 1
    // persists in the throwaway DB and is available in test 2.
    static String jwtToken;

    @Test
    @Order(1)
    void signup_shouldCreateUserAndReturn201() {
        // Build a real signup request — same JSON your Swagger UI would send
        UserSignupRequest request = new UserSignupRequest();
        request.setEmail("testuser@example.com");
        request.setName("Test User");
        request.setPhoneNumber("1234567890");
        request.setPassword("password123");
        request.setRole(Role.USER);

        // postForEntity — makes a real HTTP POST to /users/signup
        // The request body is serialized to JSON automatically.
        // The response is deserialized into UserSignupResponse.
        ResponseEntity<UserSignupResponse> response = restTemplate.postForEntity(
                "/users/signup", request, UserSignupResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser@example.com", response.getBody().getEmail());
        assertEquals("Test User", response.getBody().getName());
        assertEquals(Role.USER, response.getBody().getRole());
    }

    @Test
    @Order(2)
    void signup_duplicateEmail_shouldReturn409() {
        // Try to signup with the same email again
        UserSignupRequest request = new UserSignupRequest();
        request.setEmail("testuser@example.com");
        request.setName("Another User");
        request.setPhoneNumber("0987654321");
        request.setPassword("password456");
        request.setRole(Role.USER);

        // This should fail because of the unique email constraint on User entity
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/users/signup", request, String.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @Order(3)
    void login_withCorrectCredentials_shouldReturnToken() {
        UserLoginRequest loginRequest = new UserLoginRequest("testuser@example.com", "password123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/users/login", loginRequest, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("token"));
        assertEquals("USER", response.getBody().get("role"));

        // Save token for subsequent tests
        jwtToken = (String) response.getBody().get("token");
    }

    @Test
    @Order(4)
    void login_withWrongPassword_shouldNotReturn200() {
        UserLoginRequest loginRequest = new UserLoginRequest("testuser@example.com", "wrongpassword1");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/users/login", loginRequest, String.class);

        // Bad credentials should not succeed — the exact error code depends on
        // whether GlobalExceptionHandling catches Spring Security's AuthenticationException
        assertNotEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(5)
    void protectedEndpoint_withValidToken_shouldReturn200() {
        // HttpHeaders + HttpEntity — this is how you add custom headers to a request.
        // We set the Authorization header with our JWT token.
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // exchange() — more flexible than getForEntity. Lets you set headers, method, etc.
        // GET /bookings/me should return 200 with an empty list (no bookings yet)
        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/me", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(6)
    void protectedEndpoint_withNoToken_shouldReturn401() {
        // Call a protected endpoint without any Authorization header
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/bookings/me", String.class);

        // Your JwtAuthenticationFilter lets it pass through without auth,
        // then Spring Security blocks it because it's not authenticated
        assertTrue(
                response.getStatusCode() == HttpStatus.UNAUTHORIZED
                        || response.getStatusCode() == HttpStatus.FORBIDDEN
        );
    }

    @Test
    @Order(7)
    void protectedEndpoint_withExpiredToken_shouldReturn401() {
        // A completely made-up token that will fail JWT parsing
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.token.here");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/bookings/me", HttpMethod.GET, entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
