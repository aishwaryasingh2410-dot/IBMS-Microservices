package com.ibms.auth_service;

import com.ibms.auth_service.dto.AuthResponse;
import com.ibms.auth_service.dto.LoginRequest;
import com.ibms.auth_service.dto.RegisterRequest;
import com.ibms.auth_service.entity.User;
import com.ibms.auth_service.repository.UserRepository;
import com.ibms.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTests {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder);
    }

    @Test
    void registrationStoresHashedPasswordAndReturnsSafeResponse() {
        when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(5L);
            return saved;
        });

        AuthResponse response = authService.register(
                new RegisterRequest(" Person ", "PERSON@example.com", "StrongPass123!", "555-0101"));

        assertEquals(5L, response.id());
        assertEquals("person@example.com", response.email());
        assertEquals("Person", response.name());
        var captor = forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertNotEquals("StrongPass123!", captor.getValue().getPassword());
        assertTrue(passwordEncoder.matches("StrongPass123!", captor.getValue().getPassword()));
    }

    @Test
    void loginAcceptsMatchingPasswordAndDoesNotReturnPassword() {
        User user = new User();
        user.setId(8L);
        user.setName("Person");
        user.setEmail("person@example.com");
        user.setPassword(passwordEncoder.encode("StrongPass123!"));
        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(new LoginRequest("PERSON@example.com", "StrongPass123!"));

        assertEquals("person@example.com", response.email());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        User user = new User();
        user.setEmail("person@example.com");
        user.setPassword(passwordEncoder.encode("CorrectPass123!"));
        when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> authService.login(new LoginRequest("person@example.com", "WrongPass123!")));

        assertEquals(401, exception.getStatusCode().value());
    }
}
