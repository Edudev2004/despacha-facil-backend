package com.despachafacil.api.service;

import com.despachafacil.api.domain.User;
import com.despachafacil.api.dto.LoginRequest;
import com.despachafacil.api.dto.UserSessionResponse;
import com.despachafacil.api.repository.UserRepository;
import com.despachafacil.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserSessionResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tu cuenta de usuario se encuentra inactiva");
        }

        if (!Boolean.TRUE.equals(user.getTenant().getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El comercio asociado se encuentra inactivo");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales incorrectas");
        }

        String roleName = user.getRole().getName();
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getTenant().getId(),
                user.getTenant().getSlug(),
                roleName
        );

        return new UserSessionResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getTenant().getSlug(),
                user.getTenant().getName(),
                user.getFullName(),
                user.getEmail(),
                roleName,
                token
        );
    }
}
