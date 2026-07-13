package com.coworking.reservation.service;

import com.coworking.reservation.dto.request.LoginRequest;
import com.coworking.reservation.dto.request.RegisterRequest;
import com.coworking.reservation.dto.response.AuthResponse;
import com.coworking.reservation.entity.UserAccount;
import com.coworking.reservation.entity.enums.Role;
import com.coworking.reservation.exception.EmailAlreadyExistsException;
import com.coworking.reservation.exception.ResourceNotFoundException;
import com.coworking.reservation.repository.UserAccountRepository;
import com.coworking.reservation.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userAccountRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "Email is already registered"
            );
        }

        UserAccount userAccount = new UserAccount(
                request.name(),
                email,
                passwordEncoder.encode(request.password()),
                Role.USER
        );

        userAccountRepository.save(userAccount);

        String token = jwtService.generateToken(
                userAccount.getEmail(),
                userAccount.getRole()
        );

        return new AuthResponse(TOKEN_TYPE, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        )
                );

        String token = jwtService.generateToken(
                userAccount.getEmail(),
                userAccount.getRole()
        );

        return new AuthResponse(TOKEN_TYPE, token);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
