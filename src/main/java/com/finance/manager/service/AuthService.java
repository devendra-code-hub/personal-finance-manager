package com.finance.manager.service;

import com.finance.manager.dto.request.LoginRequest;
import com.finance.manager.dto.request.RegisterRequest;
import com.finance.manager.dto.response.ResponseDtos.*;
import com.finance.manager.entity.User;
import com.finance.manager.exception.AppExceptions.*;
import com.finance.manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration, login, logout.
 * Session management: on login, we store the userId in the HTTP session.
 * All other endpoints retrieve the userId from session to identify the user.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String SESSION_USER_ID = "USER_ID";

    public RegisterResponse register(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User with this email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());

        User saved = userRepository.save(user);
        return new RegisterResponse("User registered successfully", saved.getId());
    }

    public MessageResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(request.getUsername().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        // Create session and store userId — this is what makes subsequent requests authenticated
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SESSION_USER_ID, user.getId());

        return new MessageResponse("Login successful");
    }

    public MessageResponse logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate(); // Expire and remove session
        }
        return new MessageResponse("Logout successful");
    }

    /**
     * Helper: get authenticated user from session.
     * Called by all protected services to identify current user.
     */
    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_USER_ID) == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}