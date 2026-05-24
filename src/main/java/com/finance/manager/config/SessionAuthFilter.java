package com.finance.manager.config;

import com.finance.manager.repository.UserRepository;
import com.finance.manager.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Custom filter that validates session authentication.
 * Reads JSESSIONID from cookies and validates the session contains a valid userId.
 * This ensures Spring Security context is populated for session-based auth.
 */
@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public SessionAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Skip public endpoints
        String path = request.getRequestURI();
        if (path.equals("/api/auth/register") || path.equals("/api/auth/login")
                || path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Try to get existing session (don't create new one)
        HttpSession session = request.getSession(false);

        if (session != null) {
            Long userId = (Long) session.getAttribute(AuthService.SESSION_USER_ID);
            if (userId != null) {
                // Validate user still exists and set authentication
                userRepository.findById(userId).ifPresent(user -> {
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            null,
                            Collections.emptyList()
                        );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}