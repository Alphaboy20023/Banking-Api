package com.example.BankingApi.controllers;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BankingApi.Repositories.UserRepository;
import com.example.BankingApi.dto.TransferRequest;
import com.example.BankingApi.models.AccountModel;
import com.example.BankingApi.models.UserModel;
import com.example.BankingApi.models.AccountModel.AccountType;
import com.example.BankingApi.services.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    // Create account for user
    @PostMapping("/create/{userId}")
    public AccountModel createAccount(@PathVariable Long userId) {
        Optional<UserModel> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with id " + userId);
        }

        UserModel user = userOptional.get();

        if (user.getAccount() != null) {
            throw new RuntimeException("User already has an account");
        }

        AccountModel account = new AccountModel();
        account.setAccountNumber(accountService.generateAccountNumber());
        account.setBalance(BigDecimal.valueOf(5000.00));
        account.setAccountType(AccountType.SAVINGS);

        user.setAccount(account);
        userRepository.save(user);

        return account;
    }

    // Deposit endpoint
    @PostMapping("/{accountNumber}/deposit")
    public AccountModel deposit(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        return accountService.deposit(accountNumber, amount);
    }

    // Withdraw endpoint
    @PostMapping("/{accountNumber}/withdraw")
    public AccountModel withdraw(@PathVariable String accountNumber, @RequestParam BigDecimal amount) {
        return accountService.withdraw(accountNumber, amount);
    }

    // Transfer endpoint
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        accountService.transfer(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount());
        return ResponseEntity.ok("Transfer successful");
    }

}
