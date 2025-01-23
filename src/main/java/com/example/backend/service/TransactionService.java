package com.example.backend.service;

import com.example.backend.model.Transaction;
import com.example.backend.model.User;
import com.example.backend.model.Holdings;
import com.example.backend.repository.TransactionRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.HoldingsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final HoldingsService holdingsService;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Transactional
    public Transaction buyStock(String symbol, double quantity, double price) {
        User user = getCurrentUser();
        double total = quantity * price;
        
        if (user.getBalance() < total) {
            throw new RuntimeException("Insufficient balance");
        }

        // Update holdings first
        Holdings holdings = holdingsService.updateHoldings(symbol, quantity, price, true);
        log.info("Updated holdings after buy - Symbol: {}, Quantity: {}, Average Price: {}", 
                symbol, holdings.getQuantity(), holdings.getAveragePrice());

        // Update user balance
        user.setBalance(user.getBalance() - total);
        userRepository.save(user);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(user)
                .stockSymbol(symbol)
                .quantity(quantity)
                .price(price)
                .type("BUY")
                .timestamp(LocalDateTime.now())
                .total(total)
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction sellStock(String symbol, double quantity, double price) {
        User user = getCurrentUser();
        Holdings currentHoldings = holdingsService.getHoldingsBySymbol(symbol);
        
        if (currentHoldings.getQuantity() < quantity) {
            throw new RuntimeException("Insufficient stocks to sell");
        }

        double total = quantity * price;
        double brokerage = total * 0.03;
        double netTotal = total - brokerage;

        // Update holdings first
        Holdings holdings = holdingsService.updateHoldings(symbol, quantity, price, false);
        log.info("Updated holdings after sell - Symbol: {}, Quantity: {}, Average Price: {}", 
                symbol, holdings.getQuantity(), holdings.getAveragePrice());

        // Update user balance
        user.setBalance(user.getBalance() + netTotal);
        userRepository.save(user);

        // Create transaction record
        Transaction transaction = Transaction.builder()
                .user(user)
                .stockSymbol(symbol)
                .quantity(quantity)
                .price(price)
                .type("SELL")
                .timestamp(LocalDateTime.now())
                .total(netTotal)
                .build();

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getUserTransactions() {
        return transactionRepository.findByUserIdOrderByTimestampDesc(getCurrentUser().getId());
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
} 