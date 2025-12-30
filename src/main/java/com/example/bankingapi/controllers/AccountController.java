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
import com.example.bankingapi.Repositories.AccountRepository;

import com.example.bankingapi.dto.SetPinRequest;
import com.example.bankingapi.dto.TransferRequest;
import com.example.bankingapi.models.AccountModel;
import com.example.bankingapi.models.UserModel;
import com.example.bankingapi.models.AccountModel.AccountType;

import com.example.bankingapi.services.AccountService;
import com.example.bankingapi.services.CardService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.bankingapi.config.JwtUtil;

@Tag(name = "2. Accounts")
@RestController
@RequestMapping("/api/v1/accounts")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CardService cardService;


    private final JwtUtil jwtUtil;
    private final AccountRepository accountRepository;

    public AccountController(JwtUtil jwtUtil, AccountRepository accountRepository,
            AccountService accountService) {
        this.jwtUtil = jwtUtil;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
    }

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
        try {
            String cardNumber = (String) request.get("cardNumber");
            String cvv = (String) request.get("cvv");
            String pin = (String) request.get("pin");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String accountNumber = (String) request.get("accountNumber");

            if (cardNumber == null || cvv == null || pin == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "cardNumber, cvv, and pin are required for withdrawal"));
            }

            Map<String, Object> result = cardService.withdrawWithCard(accountNumber, cardNumber, cvv, pin, amount);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Transfer endpoint
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            boolean validPin = accountService.validatePin(request.getFromAccountNumber(), request.getPin());
            if (!validPin) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid PIN. Transaction denied.");
            }

            accountService.transfer(
                    request.getFromAccountNumber(),
                    request.getToAccountNumber(),
                    request.getAmount());

            String fromAccount = accountService.getUsernameByAccountNumber(request.getFromAccountNumber());
            String toAccount = accountService.getUsernameByAccountNumber(request.getToAccountNumber());

            String message = "Dear " + fromAccount + ", your transfer of "
                    + request.getAmount() + " to account "
                    + request.getToAccountNumber() + ", " + toAccount + " was successful.";

            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/set-pin")
    public ResponseEntity<String> setPin(@RequestBody SetPinRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            accountService.setPin(request.getAccountNumber(), request.getPin());
            return ResponseEntity.ok("PIN set successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/bvn")
    public ResponseEntity<?> getBankVerificationNumber(
            @RequestHeader("Authorization") String token,
            @RequestParam String accountNumber) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            String bvn = accountService.createBankVerificationNumber(email, accountNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bankVerificationNumber", bvn,
                    "message", "BVN generated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
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

// kyc
//
