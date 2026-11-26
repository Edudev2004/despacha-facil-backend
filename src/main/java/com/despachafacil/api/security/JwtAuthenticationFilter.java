package com.despachafacil.api.security;

import com.despachafacil.api.domain.User;
import com.despachafacil.api.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (jwt != null) {
                String userEmail = jwtService.extractUsername(jwt);
                UUID tenantId = jwtService.extractTenantId(jwt);
                String role = jwtService.extractRole(jwt);

                if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User user = userRepository.findByEmail(userEmail).orElse(null);

                    if (user != null && Boolean.TRUE.equals(user.getIsActive()) && jwtService.isTokenValid(jwt, user.getEmail())) {
                        TenantContext.setTenantId(tenantId);

                        String roleName = role != null ? role : user.getRole().getName();
                        if (!roleName.startsWith("ROLE_")) {
                            roleName = "ROLE_" + roleName;
                        }

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                List.of(new SimpleGrantedAuthority(roleName))
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.info("[SECURITY][AUTH_FILTER] Peticion autenticada [URI: {}] [User: {}] [Role: {}] [Tenant: {}]",
                                request.getRequestURI(), user.getEmail(), roleName, tenantId);
                    } else {
                        log.warn("[SECURITY][AUTH_FILTER] Token invalido o usuario inactivo para [URI: {}] [User: {}]",
                                request.getRequestURI(), userEmail);
                    }
                }
            } else {
                log.debug("[SECURITY][AUTH_FILTER] Peticion sin token [URI: {}]", request.getRequestURI());
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // 1. Authorization header: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. HttpOnly Cookie: df_token
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("df_token".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
