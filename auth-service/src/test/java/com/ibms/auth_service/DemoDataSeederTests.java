package com.ibms.auth_service;

import com.ibms.auth_service.config.DemoDataSeeder;
import com.ibms.auth_service.entity.User;
import com.ibms.auth_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DemoDataSeederTests {

    @Test
    void repairsPasswordForLegacyDemoUserWithoutPassword() {
        UserRepository userRepository = mock(UserRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User demoUser = new User();
        demoUser.setId(1L);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser));

        new DemoDataSeeder(userRepository, jdbcTemplate, passwordEncoder).run();

        verify(jdbcTemplate).update(
                org.mockito.ArgumentMatchers.eq("UPDATE users SET password = ? WHERE id = ?"),
                anyString(),
                org.mockito.ArgumentMatchers.eq(1L));
    }

    @Test
    void leavesDemoUserPasswordAloneWhenAlreadySet() {
        UserRepository userRepository = mock(UserRepository.class);
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User demoUser = new User();
        demoUser.setId(1L);
        demoUser.setPassword(passwordEncoder.encode("existing-password"));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(demoUser));

        new DemoDataSeeder(userRepository, jdbcTemplate, passwordEncoder).run();

        verifyNoInteractions(jdbcTemplate);
    }
}
