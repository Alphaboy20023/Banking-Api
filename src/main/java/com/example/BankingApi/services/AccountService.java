package com.example.BankingApi.services;

import java.math.BigDecimal;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.BankingApi.Repositories.AccountRepository;
import com.example.BankingApi.models.AccountModel;

// ALL ACCOUNT CRUD
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    private final Object lock = new Object();

    // Generate unique account number
    public String generateAccountNumber() {
        Random random = new Random();
        String accountNumber;

        synchronized (lock) {
            do {
                StringBuilder sb = new StringBuilder("14"); // prefix
                for (int i = 0; i < 8; i++) {
                    sb.append(random.nextInt(10));
                }
                accountNumber = sb.toString();
            } while (accountRepository.existsByAccountNumber(accountNumber));
        }

        return accountNumber;
    }

    // Deposit
    public AccountModel deposit(String accountNumber, BigDecimal amount) {
        AccountModel account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));

        AccountModel saved = accountRepository.save(account);
        transactionService.recordDeposit(saved, amount); // record deposit
        return saved;
    }

    // Withdraw logic
    public AccountModel withdraw(String accountNumber, BigDecimal amount) {
        AccountModel account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));

        AccountModel saved = accountRepository.save(account);
        transactionService.recordWithdrawal(saved, amount); 
        return saved;
    }

    // Transfer logic
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new RuntimeException("Cannot transfer to the same account");
        }

        AccountModel fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        AccountModel toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

       
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionService.recordTransfer(fromAccount, toAccount, amount);
    }
}
