package com.despachafacil.api.repository;

import com.despachafacil.api.domain.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    @EntityGraph(attributePaths = {"tenant", "role"})
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByTenant_IdOrderByCreatedAtDesc(UUID tenantId);
    Optional<User> findByIdAndTenant_Id(UUID id, UUID tenantId);
}

