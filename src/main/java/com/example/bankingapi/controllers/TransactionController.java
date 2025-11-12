package com.example.bankingapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import com.example.bankingapi.Repositories.TransactionRepository;
import com.example.bankingapi.models.TransactionModel;
import com.example.bankingapi.Repositories.UserRepository;
import com.example.bankingapi.models.UserModel;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all transactions — ADMIN only
    @GetMapping("/all")
    public ResponseEntity<?> getAllTransactions(Principal principal) {
        UserModel currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole().name())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied."));
        }

        List<TransactionModel> transactions = transactionRepository.findAll();

        if (transactions.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "transactions", Collections.emptyList(),
                    "message", "No transactions found"));
        }

        return ResponseEntity.ok(Map.of(
                "transactions", transactions,
                "count", transactions.size()));
    }

    // Get transactions belonging to the logged-in user
    @GetMapping
    public ResponseEntity<?> getUserTransactions(Principal principal) {
        UserModel currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<TransactionModel> userTransactions = transactionRepository
                .findByFromAccountOrToAccount(currentUser.getAccount(), currentUser.getAccount());

        if (userTransactions.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "transactions", Collections.emptyList(),
                    "message", "No transactions found for this user"));
        }

        return ResponseEntity.ok(Map.of(
                "transactions", userTransactions,
                "count", userTransactions.size()));
    }

    // Get a single transaction by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id, Principal principal) {
        UserModel currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        TransactionModel transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        // Only admin or owner can view this transaction
        boolean isOwner = transaction.getFromAccount().equals(currentUser.getAccount()) ||
                transaction.getToAccount().equals(currentUser.getAccount());

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole().name()) && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. Not your transaction."));
        }

        return ResponseEntity.ok(transaction);
    }
}

// Daily limit (e.g., ₦100,000 max).
// Flag suspicious activity (too many transfers in short time).
// send transaction alert to both users
// Redis?

// rate limiting
// email service -  send transactions to both users
