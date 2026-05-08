package com.papeleria.sales.security;

import com.papeleria.sales.model.Role;

public record AuthenticatedUser(
        String email,
        String fullName,
        Role role
) {
}
