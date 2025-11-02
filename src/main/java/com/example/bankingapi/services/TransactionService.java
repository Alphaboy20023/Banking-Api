package com.example.bankingapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bankingapi.Repositories.TransactionRepository;
import com.example.bankingapi.models.TransactionModel;
import com.example.bankingapi.models.AccountModel;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // Record a DEPOSIT transaction
    public TransactionModel recordDeposit(AccountModel account, BigDecimal amount) {
        TransactionModel tx = new TransactionModel();
        tx.setTransactionId(generateTransactionId());
        tx.setTransactionType(TransactionModel.TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setToAccount(account);
        return transactionRepository.save(tx); // return the saved transaction
    }

    // Record a WITHDRAWAL transaction
    public TransactionModel recordWithdrawal(AccountModel account, BigDecimal amount) {
        TransactionModel tx = new TransactionModel();
        tx.setTransactionId(generateTransactionId());
        tx.setTransactionType(TransactionModel.TransactionType.WITHDRAWAL);
        tx.setAmount(amount);
        tx.setFromAccount(account);
        return transactionRepository.save(tx); 
    }

    // Record a TRANSFER transaction
    public TransactionModel recordTransfer(AccountModel fromAccount, AccountModel toAccount, BigDecimal amount) {
        TransactionModel tx = new TransactionModel();
        tx.setTransactionId(generateTransactionId());
        tx.setTransactionType(TransactionModel.TransactionType.TRANSFER);
        tx.setAmount(amount);
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        return transactionRepository.save(tx); // ← Return the saved transaction
    }

    private String generateTransactionId() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return "txn-" + uuid;
    }
}
