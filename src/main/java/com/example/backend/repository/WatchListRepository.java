package com.example.backend.repository;

import com.example.backend.model.WatchList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {
    List<WatchList> findByUserId(Long userId);
} 