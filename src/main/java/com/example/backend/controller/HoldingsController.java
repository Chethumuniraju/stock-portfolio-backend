package com.example.backend.controller;

import com.example.backend.model.Holdings;
import com.example.backend.model.User;
import com.example.backend.service.HoldingsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class HoldingsController {
    private final HoldingsService holdingsService;
    private static final Logger log = LoggerFactory.getLogger(HoldingsController.class);

    @GetMapping
    public ResponseEntity<List<Holdings>> getAllHoldings() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching all holdings for user: {}", currentUser.getEmail());
        
        List<Holdings> holdings = holdingsService.getAllHoldings();
        log.info("Found {} holdings", holdings.size());
        
        holdings.forEach(h -> log.info("Holdings - Symbol: {}, Quantity: {}, Average Price: {}", 
                h.getStockSymbol(), h.getQuantity(), h.getAveragePrice()));
        
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Holdings> getHoldingsBySymbol(@PathVariable String symbol) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Fetching holdings for symbol: {} and user: {}", symbol, currentUser.getEmail());
        
        Holdings holdings = holdingsService.getHoldingsBySymbol(symbol);
        log.info("Found holdings - Quantity: {}, Average Price: {}", 
                holdings.getQuantity(), holdings.getAveragePrice());
        
        return ResponseEntity.ok(holdings);
    }
} 