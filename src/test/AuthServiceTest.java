package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.User;
import com.finance.manager.exception.AppExceptions.*;
import com.finance.manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Why @ExtendWith(MockitoExtension.class)?
 * Lightweight Mockito integration for JUnit 5 — no Spring context needed.
 * Unit tests should test ONE class in isolation, mocking all dependencies.
 * This makes tests fast (milliseconds, not seconds).
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setPassword("hashedPassword");
        testUser.setFullName("Test User");
        testUser.setPhoneNumber("+1234567890");
    }

    @Test
    void register_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RegisterResponse response = authService.register(registerRequest);

        assertEquals("User registered successfully", response.getMessage());
        assertEquals(1L, response.getUserId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsConflict() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
            () -> authService.register(registerRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(true)).thenReturn(session);

        MessageResponse response = authService.login(loginRequest, request);

        assertEquals("Login successful", response.getMessage());
        verify(session).setAttribute(AuthService.SESSION_USER_ID, 1L);
    }

    @Test
    void login_WrongPassword_ThrowsUnauthorized() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("wrongPassword");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(UnauthorizedException.class,
            () -> authService.login(loginRequest, request));
    }

    @Test
    void login_UserNotFound_ThrowsUnauthorized() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("nonexistent@example.com");
        loginRequest.setPassword("password");

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        HttpServletRequest request = mock(HttpServletRequest.class);

        assertThrows(UnauthorizedException.class,
            () -> authService.login(loginRequest, request));
    }

    @Test
    void logout_ValidSession_Invalidates() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);

        MessageResponse response = authService.logout(request);

        assertEquals("Logout successful", response.getMessage());
        verify(session).invalidate();
    }

    @Test
    void logout_NoSession_ReturnsSuccess() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession(false)).thenReturn(null);

        MessageResponse response = authService.logout(request);

        assertEquals("Logout successful", response.getMessage());
    }
}