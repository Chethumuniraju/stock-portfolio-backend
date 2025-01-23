package com.example.backend.service;

import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.dto.RegisterRequest;
import com.example.backend.dto.UserDTO;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostConstruct
    public void init() {
        // Create a test user if it doesn't exist
        if (!repository.findByEmail("test@example.com").isPresent()) {
            var user = User.builder()
                    .name("Test User")
                    .email("test@example.com")
                    .password(passwordEncoder.encode("password"))
                    .mobileNo("1234567890")
                    .balance(1000.0)
                    .role("ROLE_USER")
                    .build();
            repository.save(user);
        }
    }

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobileNo(request.getMobileNo())
                .balance(0.0)
                .role("ROLE_USER")
                .build();
        user = repository.save(user);
        var token = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDTO(user))
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            var user = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            var token = jwtService.generateToken(user);
            return AuthResponse.builder()
                    .token(token)
                    .user(mapToUserDTO(user))
                    .build();
        } catch (AuthenticationException e) {
            throw new RuntimeException("Invalid email or password");
        }
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .balance(user.getBalance())
                .build();
    }
} 