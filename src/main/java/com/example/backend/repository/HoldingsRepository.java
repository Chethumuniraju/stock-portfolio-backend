package com.example.backend.repository;

import com.example.backend.model.Holdings;
import com.example.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface HoldingsRepository extends JpaRepository<Holdings, Long> {
    Optional<Holdings> findByUserAndStockSymbol(User user, String stockSymbol);
    List<Holdings> findByUser(User user);
} 