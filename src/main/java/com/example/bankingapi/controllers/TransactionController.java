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
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // all transactions
    @GetMapping
    public ResponseEntity<?> getAllTransactions(Principal principal) {

        UserModel currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole().name())) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Access denied");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // return [], if not transactions
        List<TransactionModel> transactions = transactionRepository.findByFromAccountOrToAccount(null, null);

        if (transactions.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("transactions", Collections.emptyList());
            response.put("message", "No transactions yet");
            return ResponseEntity.ok(response);
        }

        // Otherwise return all transactions
        return ResponseEntity.ok(transactions);
    }

    // Get transaction by ID
    @GetMapping("/{id}")
    public TransactionModel getTransactionById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
    }
}


// Daily limit (e.g., ₦100,000 max).
// Flag suspicious activity (too many transfers in short time).
// send transaction alert to both users
// Redis?

