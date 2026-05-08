package com.papeleria.auth.service;

import com.papeleria.auth.dto.AuthResponse;
import com.papeleria.auth.dto.LoginRequest;
import com.papeleria.auth.dto.RegisterRequest;
import com.papeleria.auth.dto.UserResponse;
import com.papeleria.auth.exception.ResourceConflictException;
import com.papeleria.auth.exception.UnauthorizedException;
import com.papeleria.auth.model.Role;
import com.papeleria.auth.model.UserAccount;
import com.papeleria.auth.repository.UserAccountRepository;
import com.papeleria.auth.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase().trim();
        if (userAccountRepository.existsByEmail(email)) {
            throw new ResourceConflictException("Ya existe un usuario con ese correo");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setFullName(request.fullName().trim());
        userAccount.setEmail(email);
        userAccount.setPassword(passwordEncoder.encode(request.password()));
        userAccount.setRole(Role.USER);
        UserAccount savedUser = userAccountRepository.save(userAccount);

        return buildAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount userAccount = userAccountRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.password(), userAccount.getPassword())) {
            throw new UnauthorizedException("Credenciales invalidas");
        }

        return buildAuthResponse(userAccount);
    }

    public UserResponse getCurrentUser(String email) {
        UserAccount userAccount = userAccountRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Usuario no encontrado"));
        return mapUser(userAccount);
    }

    private AuthResponse buildAuthResponse(UserAccount userAccount) {
        return new AuthResponse(jwtService.generateToken(userAccount), mapUser(userAccount));
    }

    private UserResponse mapUser(UserAccount userAccount) {
        return new UserResponse(
                userAccount.getId(),
                userAccount.getFullName(),
                userAccount.getEmail(),
                userAccount.getRole(),
                userAccount.getCreatedAt()
        );
    }
}
