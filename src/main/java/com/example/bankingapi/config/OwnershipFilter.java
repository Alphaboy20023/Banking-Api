package com.example.bankingapi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.bankingapi.Repositories.AccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

@Component
public class OwnershipFilter extends OncePerRequestFilter {

    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OwnershipFilter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    private static final List<String> PROTECTED_PATHS = List.of(
            "/api/accounts",
            "/api/transactions");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // Wrap the request to allow multiple reads
        MultipleReader cachedRequest = new MultipleReader(request);

        String path = cachedRequest.getRequestURI();
        boolean isProtected = PROTECTED_PATHS.stream().anyMatch(path::startsWith);

        if (isProtected) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()) {
                String authenticatedEmail = auth.getName();
                String requestAccountNumber = null;

                // Try query parameter first
                requestAccountNumber = cachedRequest.getParameter("accountNumber");

                // If not found, check request body
                if (requestAccountNumber == null) {
                    String body = cachedRequest.getBody();

                    if (body != null && !body.isEmpty()) {
                        try {
                            
                            JsonNode jsonNode = objectMapper.readTree(body);
                            if (jsonNode.has("accountNumber")) {
                                requestAccountNumber = jsonNode.get("accountNumber").asText();
                            }

                            else if (jsonNode.has("fromAccountNumber")) {
                                requestAccountNumber = jsonNode.get("fromAccountNumber").asText();
                            }

                        } catch (Exception e) {
                            // Continue without check if parsing fails
                        }
                    }
                }

                if (requestAccountNumber != null) {
                    boolean isOwner = accountRepository.existsByAccountNumberAndUserEmail(requestAccountNumber,
                            authenticatedEmail);

                    if (!isOwner) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("Access denied: Invalid or missing token.");
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(cachedRequest, response);
    }
}