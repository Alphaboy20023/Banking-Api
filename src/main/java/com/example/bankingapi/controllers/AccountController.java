package com.example.bankingapi.controllers;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.bankingapi.Repositories.UserRepository;
import com.example.bankingapi.dto.SetPinRequest;
import com.example.bankingapi.dto.TransferRequest;
import com.example.bankingapi.models.AccountModel;
import com.example.bankingapi.models.UserModel;
import com.example.bankingapi.models.AccountModel.AccountType;
import com.example.bankingapi.services.AccountService;

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
    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@RequestBody Map<String, Object> request) {
        String accountNumber = (String) request.get("accountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        AccountModel updatedAccount = accountService.deposit(accountNumber, amount);

        BigDecimal newBalance = updatedAccount.getBalance();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deposit of " + amount + " to account " + accountNumber + " was successful.");
        response.put("newBalance", newBalance);
        response.put("accountNumber", accountNumber);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@RequestBody Map<String, Object> request) {
        String accountNumber = (String) request.get("accountNumber");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());

        AccountModel updatedAccount = accountService.deposit(accountNumber, amount);

        BigDecimal newBalance = updatedAccount.getBalance();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Deposit of " + amount + " to account " + accountNumber + " was successful.");
        response.put("newBalance", newBalance);
        response.put("accountNumber", accountNumber);

        return ResponseEntity.ok(response);
    }

    // Transfer endpoint
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {

        boolean validPin = accountService.validatePin(request.getFromAccountNumber(), request.getPin());
        if (!validPin) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid PIN. Transaction denied.");
        }

        accountService.transfer(
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                request.getAmount());

        String userA = accountService.getUsernameByAccountNumber(request.getFromAccountNumber());
        String userB = accountService.getUsernameByAccountNumber(request.getToAccountNumber());

        String message = "Dear " + userA + ", your transfer of "
                + request.getAmount() + " to account "
                + request.getToAccountNumber() + ", " + userB + " was successful.";

        return ResponseEntity.ok(message);
    }

    @PostMapping("/set-pin")
    public ResponseEntity<String> setPin(@RequestBody SetPinRequest request) {
        try {
            accountService.setPin(request.getAccountNumber(), request.getPin());
            return ResponseEntity.ok("PIN set successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}

// to test api/accounts/transfer, use payload:
// {
// "fromAccountNumber":1411613301,
// "toAccountNumber":1422197261,
// "pin":"4646",
// "amount":"800"
// }
