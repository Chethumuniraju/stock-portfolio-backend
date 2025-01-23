package com.example.backend.service;

import com.example.backend.model.Holdings;
import com.example.backend.model.User;
import com.example.backend.repository.HoldingsRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoldingsService {
    private final HoldingsRepository holdingsRepository;
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(HoldingsService.class);

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<Holdings> getAllHoldings() {
        User user = getCurrentUser();
        List<Holdings> holdings = holdingsRepository.findByUser(user);
        log.info("Found {} holdings for user {}", holdings.size(), user.getEmail());
        return holdings.stream()
                .filter(h -> h.getQuantity() > 0)  // Only return holdings with positive quantity
                .collect(Collectors.toList());
    }

    public List<Holdings> getHoldingsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        log.info("Fetching holdings for user ID: {}", userId);
        
        List<Holdings> holdings = holdingsRepository.findByUser(user);
        log.info("Found {} holdings for shared portfolio", holdings.size());
        
        return holdings.stream()
                .filter(h -> h.getQuantity() > 0)
                .collect(Collectors.toList());
    }

    public Holdings getHoldingsBySymbol(String symbol) {
        User user = getCurrentUser();
        log.info("Fetching holdings for symbol: {} and user: {}", symbol, user.getEmail());
        return holdingsRepository.findByUserAndStockSymbol(user, symbol)
                .orElse(Holdings.builder()
                        .stockSymbol(symbol)
                        .quantity(0.0)
                        .averagePrice(0.0)
                        .build());
    }

    @Transactional
    public Holdings updateHoldings(String symbol, double quantity, double price, boolean isBuy) {
        User user = getCurrentUser();
        Holdings holdings = holdingsRepository.findByUserAndStockSymbol(user, symbol)
                .orElse(Holdings.builder()
                        .user(user)
                        .stockSymbol(symbol)
                        .quantity(0.0)
                        .averagePrice(0.0)
                        .build());

        if (isBuy) {
            double newTotalValue = (holdings.getQuantity() * holdings.getAveragePrice()) + (quantity * price);
            double newTotalQuantity = holdings.getQuantity() + quantity;
            holdings.setQuantity(newTotalQuantity);
            holdings.setAveragePrice(newTotalValue / newTotalQuantity);
        } else {
            holdings.setQuantity(holdings.getQuantity() - quantity);
        }

        if (holdings.getQuantity() <= 0) {
            holdingsRepository.delete(holdings);
            return Holdings.builder()
                    .stockSymbol(symbol)
                    .quantity(0.0)
                    .averagePrice(0.0)
                    .build();
        }

        log.info("Updating holdings for symbol: {}, new quantity: {}, new average price: {}", 
                symbol, holdings.getQuantity(), holdings.getAveragePrice());
        return holdingsRepository.save(holdings);
    }
} 