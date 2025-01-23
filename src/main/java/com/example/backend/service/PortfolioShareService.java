package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.model.PortfolioShare;
import com.example.backend.model.Holdings;
import com.example.backend.repository.PortfolioShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioShareService {
    private final PortfolioShareRepository portfolioShareRepository;
    private final HoldingsService holdingsService;

    public PortfolioShare createShareLink(User user) {
        PortfolioShare share = new PortfolioShare();
        share.setUser(user);
        share.setShareId(UUID.randomUUID().toString());
        share.setCreatedAt(LocalDateTime.now());
        share.setExpiresAt(LocalDateTime.now().plusDays(7));
        share.setActive(true);
        return portfolioShareRepository.save(share);
    }

    public Object getSharedPortfolio(String shareId) {
        PortfolioShare share = portfolioShareRepository.findByShareIdAndIsActiveTrue(shareId)
            .orElseThrow(() -> new RuntimeException("Share link not found or expired"));

        if (share.getExpiresAt().isBefore(LocalDateTime.now())) {
            share.setActive(false);
            portfolioShareRepository.save(share);
            throw new RuntimeException("Share link has expired");
        }

        List<Holdings> userHoldings = holdingsService.getAllHoldingsForUser(share.getUser());
        
        return new Object() {
            public final String userName = share.getUser().getName() != null ? 
                share.getUser().getName() : share.getUser().getEmail();
            public final LocalDateTime expiresAt = share.getExpiresAt();
            public final List<Holdings> holdings = userHoldings;
        };
    }

    public void deactivateShareLink(String shareId, User user) {
        PortfolioShare share = portfolioShareRepository.findByShareId(shareId)
            .orElseThrow(() -> new RuntimeException("Share link not found"));

        if (!share.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to deactivate this share link");
        }

        share.setActive(false);
        portfolioShareRepository.save(share);
    }
}