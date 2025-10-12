package com.example.bankingapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.example.bankingapi.Repositories.TransactionRepository;
import com.example.bankingapi.models.TransactionModel;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    // all transactions
    @GetMapping
    public List<TransactionModel> getAllTransactions() {
        return transactionRepository.findAll();
    }

    //Get transaction by ID
    @GetMapping("/{id}")
    public TransactionModel getTransactionById(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id " + id));
    }
}
