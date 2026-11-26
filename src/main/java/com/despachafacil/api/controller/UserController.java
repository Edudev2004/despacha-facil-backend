package com.despachafacil.api.controller;

import com.despachafacil.api.dto.CreateTeamMemberRequest;
import com.despachafacil.api.dto.TeamMemberResponse;
import com.despachafacil.api.dto.UpdateTeamMemberRequest;
import com.despachafacil.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers() {
        return ResponseEntity.ok(userService.getTeamMembers());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamMemberResponse> createTeamMember(
            @Valid @RequestBody CreateTeamMemberRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createTeamMember(request));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamMemberResponse> updateTeamMember(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateTeamMemberRequest request
    ) {
        return ResponseEntity.ok(userService.updateTeamMember(userId, request));
    }
}
