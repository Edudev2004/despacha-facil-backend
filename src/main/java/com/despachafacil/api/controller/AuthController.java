package com.despachafacil.api.controller;

import com.despachafacil.api.domain.User;
import com.despachafacil.api.dto.LoginRequest;
import com.despachafacil.api.dto.RegisterTenantRequest;
import com.despachafacil.api.dto.SlugCheckResponse;
import com.despachafacil.api.dto.UserSessionResponse;
import com.despachafacil.api.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/check-slug")
    public ResponseEntity<SlugCheckResponse> checkSlug(@RequestParam String slug) {
        return ResponseEntity.ok(authService.checkSlugAvailability(slug));
    }

    @PostMapping("/register-tenant")
    public ResponseEntity<UserSessionResponse> registerTenant(
            @Valid @RequestBody RegisterTenantRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        UserSessionResponse session = authService.registerTenant(request);
        ResponseCookie cookie = createAuthCookie(session.token(), 30L * 24 * 3600, httpRequest);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PostMapping("/login")
    public ResponseEntity<UserSessionResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        UserSessionResponse session = authService.login(request);
        long maxAgeSeconds = Boolean.TRUE.equals(request.rememberMe()) ? 30L * 24 * 3600 : 24L * 3600;
        ResponseCookie cookie = createAuthCookie(session.token(), maxAgeSeconds, httpRequest);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(session);
    }

    @GetMapping("/me")
    public ResponseEntity<UserSessionResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        UserSessionResponse session = new UserSessionResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getTenant().getSlug(),
                user.getTenant().getName(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getName(),
                null
        );

        return ResponseEntity.ok(session);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse response) {
        ResponseCookie cookie = createAuthCookie("", 0L, httpRequest);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    private ResponseCookie createAuthCookie(String token, long maxAgeSeconds, HttpServletRequest request) {
        String host = request.getServerName();
        boolean isSecure = request.isSecure() || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from("df_token", token)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAgeSeconds);

        if (host != null && host.endsWith("despachafacil.com")) {
            builder.domain("despachafacil.com");
            builder.secure(true);
            builder.sameSite("Lax");
        } else {
            builder.secure(isSecure);
            builder.sameSite("Lax");
        }

        return builder.build();
    }
}
