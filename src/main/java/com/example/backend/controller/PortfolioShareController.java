package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.PortfolioShareService;
import com.example.backend.service.HoldingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio-share")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "https://stock-portfolio-frontend.onrender.com"}, allowCredentials = "true")
public class PortfolioShareController {
    private final PortfolioShareService portfolioShareService;
    private final HoldingsService holdingsService;

    @PostMapping("/create")
    public ResponseEntity<?> createShareLink() {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(portfolioShareService.createShareLink(currentUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{shareId}")
    public ResponseEntity<?> getSharedPortfolio(@PathVariable String shareId) {
        try {
            return ResponseEntity.ok(portfolioShareService.getSharedPortfolio(shareId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{shareId}")
    public ResponseEntity<?> deactivateShareLink(@PathVariable String shareId) {
        try {
            User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            portfolioShareService.deactivateShareLink(shareId, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
} 