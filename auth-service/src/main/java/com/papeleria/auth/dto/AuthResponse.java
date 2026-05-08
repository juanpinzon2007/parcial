package com.papeleria.auth.dto;

public record AuthResponse(
        String token,
        UserResponse user
) {
}
