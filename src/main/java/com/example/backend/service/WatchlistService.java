package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.model.WatchList;
import com.example.backend.repository.WatchListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {
    private final WatchListRepository watchListRepository;
    private static final Logger log = LoggerFactory.getLogger(WatchlistService.class);

    private User getCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof User) {
                return (User) auth.getPrincipal();
            }
            return null;
        } catch (Exception e) {
            log.warn("Error getting current user: {}", e.getMessage());
            return null;
        }
    }

    public List<WatchList> getUserWatchlists() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Authentication required to fetch watchlists");
        }
        return watchListRepository.findByUserId(currentUser.getId());
    }

    public WatchList getWatchlistById(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Authentication required to access watchlist");
        }
        
        WatchList watchlist = watchListRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Watchlist not found"));
            
        // Only allow access if the user owns the watchlist
        if (!watchlist.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only access your own watchlists");
        }
        
        return watchlist;
    }

    public WatchList createWatchlist(WatchList watchlist) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Authentication required to create watchlist");
        }
        watchlist.setUser(currentUser);
        if (watchlist.getStockSymbols() == null) {
            watchlist.setStockSymbols(new ArrayList<>());
        }
        return watchListRepository.save(watchlist);
    }

    public WatchList addStockToWatchlist(Long watchlistId, String symbol) {
        WatchList watchlist = watchListRepository.findById(watchlistId)
                .orElseThrow(() -> new RuntimeException("Watchlist not found"));

        if (!watchlist.getStockSymbols().contains(symbol)) {
            watchlist.getStockSymbols().add(symbol);
            return watchListRepository.save(watchlist);
        }
        return watchlist;
    }

    public WatchList removeStockFromWatchlist(Long watchlistId, String symbol) {
        WatchList watchlist = watchListRepository.findById(watchlistId)
                .orElseThrow(() -> new RuntimeException("Watchlist not found"));

        watchlist.getStockSymbols().remove(symbol);
        return watchListRepository.save(watchlist);
    }

    public void deleteWatchlist(Long id) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("Authentication required to delete watchlist");
        }
        
        WatchList watchlist = watchListRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Watchlist not found"));
            
        // Only allow deletion if the user owns the watchlist
        if (!watchlist.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own watchlists");
        }
        
        watchListRepository.deleteById(id);
    }
} 