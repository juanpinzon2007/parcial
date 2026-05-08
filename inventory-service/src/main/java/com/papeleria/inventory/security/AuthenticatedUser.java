package com.papeleria.inventory.security;

import com.papeleria.inventory.model.Role;

public record AuthenticatedUser(
        String email,
        String fullName,
        Role role
) {
}
