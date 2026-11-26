package com.despachafacil.api.service;

import com.despachafacil.api.domain.Role;
import com.despachafacil.api.domain.Tenant;
import com.despachafacil.api.domain.User;
import com.despachafacil.api.dto.CreateTeamMemberRequest;
import com.despachafacil.api.dto.TeamMemberResponse;
import com.despachafacil.api.dto.UpdateTeamMemberRequest;
import com.despachafacil.api.repository.RoleRepository;
import com.despachafacil.api.repository.TenantRepository;
import com.despachafacil.api.repository.UserRepository;
import com.despachafacil.api.security.TenantContext;
import com.despachafacil.api.security.TenantValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenantValidator tenantValidator;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder,
            TenantValidator tenantValidator
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
        this.tenantValidator = tenantValidator;
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers() {
        UUID tenantId = TenantContext.getTenantId();
        tenantValidator.validateTenantId(tenantId);

        return userRepository.findAllByTenant_IdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TeamMemberResponse createTeamMember(CreateTeamMemberRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        tenantValidator.validateTenantId(tenantId);

        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya está registrado");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comercio no encontrado"));

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no válido"));

        User newUser = new User();
        newUser.setTenant(tenant);
        newUser.setRole(role);
        newUser.setFullName(request.fullName().trim());
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(request.password()));
        newUser.setIsActive(true);

        User saved = userRepository.save(newUser);
        log.info("[TEAM][CREATE] [Tenant: {}] Colaborador registrado exitosamente [Email: {}] [Rol: {}]",
                tenantId, saved.getEmail(), saved.getRole().getName());
        return toResponse(saved);
    }

    @Transactional
    public TeamMemberResponse updateTeamMember(UUID userId, UpdateTeamMemberRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        tenantValidator.validateTenantId(tenantId);

        User user = userRepository.findByIdAndTenant_Id(userId, tenantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Colaborador no encontrado en este comercio"));

        tenantValidator.validateTenantAccess(user);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        if (request.role() != null && !request.role().isBlank()) {
            Role role = roleRepository.findByName(request.role())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no válido"));
            user.setRole(role);
        }

        if (request.isActive() != null) {
            user.setIsActive(request.isActive());
        }

        User updated = userRepository.save(user);
        return toResponse(updated);
    }

    private TeamMemberResponse toResponse(User user) {
        return new TeamMemberResponse(
                user.getId(),
                user.getTenant().getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().getName(),
                user.getIsActive(),
                user.getCreatedAt()
        );
    }
}
