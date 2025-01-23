package com.example.backend.repository;

import com.example.backend.model.PortfolioShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PortfolioShareRepository extends JpaRepository<PortfolioShare, Long> {
    Optional<PortfolioShare> findByShareId(String shareId);
    Optional<PortfolioShare> findByShareIdAndIsActiveTrue(String shareId);
}