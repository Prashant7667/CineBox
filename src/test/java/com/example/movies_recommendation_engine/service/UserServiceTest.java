package com.example.movies_recommendation_engine.service;

import com.example.movies_recommendation_engine.dto.UserSignupRequest;
import com.example.movies_recommendation_engine.dto.UserSignupResponse;
import com.example.movies_recommendation_engine.models.Role;
import com.example.movies_recommendation_engine.models.User;
import com.example.movies_recommendation_engine.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) — activates Mockito annotations.
// Without this, @Mock and @InjectMocks are just ignored.
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    // @Mock — creates a fake UserRepository.
    // It doesn't connect to any database. All its methods return null by default.
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // @InjectMocks — creates a REAL UserService, but injects the @Mock objects above
    // into its constructor. So userService.saveUser() runs real logic,
    // but when it calls userRepository.save(), it hits the fake.
    @InjectMocks
    private UserService userService;

    @Test
    void saveUser_shouldEncodePasswordAndSaveToRepo() {
        // Arrange — set up the test data and mock behavior
        UserSignupRequest request = new UserSignupRequest();
        request.setEmail("test@example.com");
        request.setName("Test User");
        request.setPhoneNumber("1234567890");
        request.setPassword("password123");
        request.setRole(Role.USER);

        // when().thenReturn() — tells the mock:
        // "When someone calls passwordEncoder.encode("password123"), return "hashed_pw"
        when(passwordEncoder.encode("password123")).thenReturn("hashed_pw");

        // any(User.class) — matches ANY User object passed to save().
        // We use this because we don't know the exact User object that will be created.
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // Act — call the real method
        UserSignupResponse response = userService.saveUser(request);

        // Assert — verify the response
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertEquals(Role.USER, response.getRole());

        // verify() — asserts that a method WAS called on the mock.
        // This proves that our service actually called passwordEncoder.encode()
        // and userRepository.save().
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));

        // ArgumentCaptor — captures the actual argument passed to a mock method.
        // This lets us inspect WHAT was saved to the repository.
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        // Verify the password was encoded, not stored as plain text
        assertEquals("hashed_pw", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    void saveUser_shouldReturnResponseWithCorrectFields() {
        UserSignupRequest request = new UserSignupRequest();
        request.setEmail("admin@example.com");
        request.setName("Admin");
        request.setPhoneNumber("9876543210");
        request.setPassword("admin12345");
        request.setRole(Role.ADMIN);

        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });

        UserSignupResponse response = userService.saveUser(request);

        assertEquals("admin@example.com", response.getEmail());
        assertEquals("Admin", response.getName());
        assertEquals("9876543210", response.getPhoneNumber());
        assertEquals(Role.ADMIN, response.getRole());
    }
}
