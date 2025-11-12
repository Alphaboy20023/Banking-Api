package com.example.bankingapi.RateLimiting;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.thymeleaf.standard.expression.Each;

public class RateLimiter {
    private final int maxRequests; // Maximum requests allowed
    private final long timeWindowSeconds; // time window in seconds
    private final ConcurrentHashMap<String, RequestBucket> buckets;

    // Constructor: Set your limits here
    public RateLimiter(int maxRequests, long timeWindowSeconds) {
        this.maxRequests = maxRequests;
        this.timeWindowSeconds = timeWindowSeconds;
        this.buckets = new ConcurrentHashMap<>();
    }

    // Check if user can make a request
    public boolean allowRequest(String userId) {
        RequestBucket bucket = buckets.computeIfAbsent(
                userId,
                k -> new RequestBucket(maxRequests, timeWindowSeconds));
        return bucket.tryConsume();
    }

    // Get remaining requests for user
    public int getRemainingRequests(String userId) {
        RequestBucket bucket = buckets.get(userId);
        return bucket != null ? bucket.getAvailableTokens() : maxRequests;
    }

    // Inner class representing a request bucket for each user
    private static class RequestBucket {
        private int tokens;
        private final int maxTokens;
        private final long refillIntervalSeconds;
        private Instant lastRefill;

        public RequestBucket(int maxTokens, long refillIntervalSeconds) {
            this.maxTokens = maxTokens;
            this.tokens = maxTokens;
            this.refillIntervalSeconds = refillIntervalSeconds;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refillTokens();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        public synchronized int getAvailableTokens() {
            refillTokens();
            return tokens;
        }

        private void refillTokens() {
            Instant now = Instant.now();
            long secondsPassed = now.getEpochSecond() - lastRefill.getEpochSecond();

            if (secondsPassed >= refillIntervalSeconds) {
                tokens = maxTokens;
                lastRefill = now;
            }
        }
    }

    // **What this does:**-

    // Gives each user a"bucket" with tokens-
    // Each request takes 1 token-
    // Tokens refill
    // after time window

}
