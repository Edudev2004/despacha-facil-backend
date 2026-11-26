package com.despachafacil.api.service;

import com.despachafacil.api.domain.Plan;
import com.despachafacil.api.domain.Role;
import com.despachafacil.api.domain.Tenant;
import com.despachafacil.api.domain.User;
import com.despachafacil.api.dto.LoginRequest;
import com.despachafacil.api.dto.RegisterTenantRequest;
import com.despachafacil.api.dto.SlugCheckResponse;
import com.despachafacil.api.dto.UserSessionResponse;
import com.despachafacil.api.repository.PlanRepository;
import com.despachafacil.api.repository.RoleRepository;
import com.despachafacil.api.repository.TenantRepository;
import com.despachafacil.api.repository.UserRepository;
import com.despachafacil.api.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final RoleRepository roleRepository;
    private final PlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PlanRepository planRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.planRepository = planRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public SlugCheckResponse checkSlugAvailability(String rawSlug) {
        if (rawSlug == null || rawSlug.isBlank()) {
            return new SlugCheckResponse("", false);
        }
        String normalized = rawSlug.trim().toLowerCase(Locale.ROOT);
        boolean available = !tenantRepository.existsBySlug(normalized);
        return new SlugCheckResponse(normalized, available);
    }

    @Transactional
    public UserSessionResponse registerTenant(RegisterTenantRequest request) {
        String normalizedSlug = request.slug().trim().toLowerCase(Locale.ROOT);

        if (tenantRepository.existsBySlug(normalizedSlug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El subdominio/slug ya se encuentra registrado");
        }

        if (userRepository.existsByEmail(request.adminEmail().trim().toLowerCase(Locale.ROOT))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo electrónico ya tiene una cuenta asociada");
        }

        Plan initialPlan = planRepository.findByName("Emprendedor")
                .or(() -> planRepository.findById(1))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Plan inicial no configurado"));

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Rol de administrador no encontrado"));

        Tenant tenant = new Tenant();
        tenant.setPlan(initialPlan);
        tenant.setName(request.storeName().trim());
        tenant.setSlug(normalizedSlug);
        tenant.setBusinessType(request.businessType() != null && !request.businessType().isBlank() ? request.businessType().trim() : "RETAIL");
        tenant.setWhatsappNumber(request.whatsappNumber().trim());
        tenant.setCurrency("PEN");
        tenant.setIsActive(true);

        Tenant savedTenant = tenantRepository.save(tenant);

        User adminUser = new User();
        adminUser.setTenant(savedTenant);
        adminUser.setRole(adminRole);
        adminUser.setFullName(request.adminFullName().trim());
        adminUser.setEmail(request.adminEmail().trim().toLowerCase(Locale.ROOT));
        adminUser.setPasswordHash(passwordEncoder.encode(request.password()));
        adminUser.setIsActive(true);

        User savedUser = userRepository.save(adminUser);

        String token = jwtService.generateToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedTenant.getId(),
                savedTenant.getSlug(),
                adminRole.getName()
        );

        return new UserSessionResponse(
                savedUser.getId(),
                savedTenant.getId(),
                savedTenant.getSlug(),
                savedTenant.getName(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                adminRole.getName(),
                token
        );
    }

    public UserSessionResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
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
