package com.example.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.backend.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class StockController {
    private final StockService stockService;
    private static final Logger log = LoggerFactory.getLogger(StockController.class);
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${alphavantage.api.key}")
    private String apiKey;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStocks(@RequestParam String symbol) {
        try {
            log.info("Searching for symbol: {}", symbol);
            Map<String, Object> result = stockService.searchStocks(symbol);
            log.info("Search results: {}", result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error searching stocks: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{symbol}/data")
    public ResponseEntity<Object> getStockData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1h") String interval) {
        return ResponseEntity.ok(stockService.getStockData(symbol, interval));
    }

    @GetMapping("/{symbol}/quote")
    public ResponseEntity<Object> getQuote(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getQuote(symbol));
    }

    @GetMapping("/top-movers")
    public ResponseEntity<Object> getTopMovers() {
        try {
            log.info("Fetching top movers");
            String url = String.format("https://www.alphavantage.co/query?function=TOP_GAINERS_LOSERS&apikey=%s", apiKey);
            Object result = restTemplate.getForObject(url, Object.class);
            log.info("Successfully fetched top movers");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching top movers: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{symbol}/fundamentals")
    public ResponseEntity<Object> getFundamentals(@PathVariable String symbol) {
        try {
            log.info("Fetching fundamentals for symbol: {}", symbol);
            String url = String.format("https://www.alphavantage.co/query?function=OVERVIEW&symbol=%s&apikey=%s", symbol, apiKey);
            Object result = restTemplate.getForObject(url, Object.class);
            log.info("Successfully fetched fundamentals for {}", symbol);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching fundamentals: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{symbol}/financials")
    public ResponseEntity<Object> getFinancials(@PathVariable String symbol) {
        try {
            log.info("Fetching financials for symbol: {}", symbol);
            String url = String.format("https://www.alphavantage.co/query?function=CASH_FLOW&symbol=%s&apikey=%s", symbol, apiKey);
            Object result = restTemplate.getForObject(url, Object.class);
            log.info("Successfully fetched financials for {}", symbol);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching financials: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/news")
    public ResponseEntity<Object> getMarketNews() {
        try {
            log.info("Fetching market news from Alpha Vantage");
            String url = String.format("https://www.alphavantage.co/query?function=NEWS_SENTIMENT&apikey=%s", apiKey);
            
            log.info("Making request to URL: {}", url);
            Object result = restTemplate.getForObject(url, Object.class);
            
            if (result != null) {
                log.info("Received response from Alpha Vantage: {}", result);
                
                // Check if we got a rate limit error
                if (result instanceof Map && ((Map<?, ?>) result).containsKey("Note")) {
                    String errorMessage = ((Map<?, ?>) result).get("Note").toString();
                    log.warn("Alpha Vantage API limit reached: {}", errorMessage);
                    return ResponseEntity.status(429).body(errorMessage);
                }
                
                return ResponseEntity.ok(result);
            } else {
                log.warn("Received null response from Alpha Vantage");
                return ResponseEntity.badRequest().body("No data received from Alpha Vantage");
            }
        } catch (Exception e) {
            log.error("Error fetching market news: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching market news: " + e.getMessage());
        }
    }
} 