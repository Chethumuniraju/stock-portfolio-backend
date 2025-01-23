package com.example.backend.controller;

import com.example.backend.model.Transaction;
import com.example.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(@RequestBody Map<String, Object> request) {
        try {
            String symbol = (String) request.get("symbol");
            double quantity = Double.parseDouble(request.get("quantity").toString());
            double price = Double.parseDouble(request.get("price").toString());
            
            Transaction transaction = transactionService.buyStock(symbol, quantity, price);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(@RequestBody Map<String, Object> request) {
        try {
            String symbol = (String) request.get("symbol");
            double quantity = Double.parseDouble(request.get("quantity").toString());
            double price = Double.parseDouble(request.get("price").toString());
            
            Transaction transaction = transactionService.sellStock(symbol, quantity, price);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions() {
        return ResponseEntity.ok(transactionService.getUserTransactions());
    }
} 