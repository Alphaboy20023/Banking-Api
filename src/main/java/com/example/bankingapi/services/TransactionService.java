package com.example.bankingapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.bankingapi.Repositories.TransactionRepository;
import com.example.bankingapi.models.TransactionModel;
import com.example.bankingapi.models.AccountModel;

import java.math.BigDecimal;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // Record a DEPOSIT transaction
    public void recordDeposit(AccountModel account, BigDecimal amount) {
        TransactionModel tx = new TransactionModel();
        tx.setTransactionType(TransactionModel.TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setToAccount(account);
        transactionRepository.save(tx);
    }

    // Record a WITHDRAWAL transaction
    public void recordWithdrawal(AccountModel account, BigDecimal amount) {
        TransactionModel tx = new TransactionModel();
        tx.setTransactionType(TransactionModel.TransactionType.WITHDRAWAL);
        tx.setAmount(amount);
        tx.setFromAccount(account);
        transactionRepository.save(tx);
    }

    // Record a TRANSFER transaction
    public void recordTransfer(AccountModel fromAccount, AccountModel toAccount, BigDecimal amount) {
        TransactionModel tx = new TransactionModel();
        tx.setTransactionType(TransactionModel.TransactionType.TRANSFER);
        tx.setAmount(amount);
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        transactionRepository.save(tx);
    }
}
