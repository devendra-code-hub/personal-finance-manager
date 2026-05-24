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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String SESSION_USER_ID = "USER_ID";

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().toLowerCase().trim())) {
            throw new DuplicateResourceException("User with this email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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

        // Invalidate any existing session first to prevent session fixation
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Create new session
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(SESSION_USER_ID, user.getId());

        // Also set Spring Security context for this request
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, Collections.emptyList()
            );
        SecurityContextHolder.getContext().setAuthentication(auth);

        return new MessageResponse("Login successful");
    }

    public MessageResponse logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return new MessageResponse("Logout successful");
    }

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