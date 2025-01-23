package com.example.backend.service;

import com.example.backend.dto.UserDTO;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private User getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            log.error("No authenticated user found");
            throw new RuntimeException("No authenticated user found");
        }
        return (User) auth.getPrincipal();
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNo(user.getMobileNo())
                .balance(user.getBalance())
                .build();
    }

    @Transactional
    public User topUpBalance(double amount) {
        log.info("Attempting to top up balance by: {}", amount);
        if (amount <= 0) {
            log.error("Invalid amount: {}", amount);
            throw new RuntimeException("Amount must be greater than 0");
        }

        User user = getCurrentUser();
        log.info("Current user: {}", user.getEmail());
        user.setBalance(user.getBalance() + amount);
        User savedUser = userRepository.save(user);
        log.info("Balance updated successfully. New balance: {}", savedUser.getBalance());
        return savedUser;
    }
} 