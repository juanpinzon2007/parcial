package com.papeleria.auth.dto;

import com.papeleria.auth.model.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        Instant createdAt
) {
}
