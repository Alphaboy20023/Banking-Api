package com.example.bankingapi.RateLimiting;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiter rateLimiter;

    public RateLimitInterceptor(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get user ID (from IP address)
        String userId = getClientIP(request);

        // Check if user exceeded limit
        if (!rateLimiter.allowRequest(userId)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Try again later.\"}");
            response.getWriter().flush();
            return false; // Block the request
        }

        // Add header showing remaining requests
        int remaining = rateLimiter.getRemainingRequests(userId);
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        // System.out.println("Request allowed. Remaining: " + remaining);

        return true; // Allow the request
    }

    // Get user's IP address
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
