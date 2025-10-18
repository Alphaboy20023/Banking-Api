package com.example.bankingapi.controllers;

import org.springframework.web.bind.annotation.DeleteMapping;

//  mvn spring-boot:run

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankingapi.models.AccountModel;
import com.example.bankingapi.models.AccountModel.AccountType;
import com.example.bankingapi.models.UserModel;

import com.example.bankingapi.services.AccountService;

import jakarta.validation.Valid;

// import com.example.BankingApi.Repositories.AccountRepository;
import com.example.bankingapi.Repositories.UserRepository;
import com.example.bankingapi.config.JwtUtil;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody @Valid UserModel user) {
        try {
            if (user.getRole() == null) {
                user.setRole(UserModel.Role.USER);
            }

            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("Error", "Email already exists"));
            }

            if (user.getPassword().length() < 8) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("Error", "password cannot be less than 8 characters"));
            }

            // encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // generate account Number
            AccountModel account = new AccountModel();
            account.setAccountNumber(accountService.generateAccountNumber()); // generate acct no
            account.setBalance(BigDecimal.valueOf(5_000.00));
            account.setAccountType(AccountType.SAVINGS);

            // link user to account
            user.setAccount(account);

            // save
            UserModel savedUser = userRepository.save(user);

            String token = jwtUtil.generateToken(savedUser.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("user", savedUser);
            response.put("message", "User Created successfully");
            response.put("token", token);

            return ResponseEntity
                    .status(201)
                    .body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserModel user) {
        try {
            // Check email
            Optional<UserModel> existingUserOpt = userRepository.findByEmail(user.getEmail());
            if (existingUserOpt.isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Email does not exist"));
            }

            UserModel existingUser = existingUserOpt.get();

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Password is required"));
            }

            if (user.getPassword().length() < 8) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Password cannot be less than 8 characters"));
            }

            // compare new to old
            if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Incorrect password"));
            }

            String token = jwtUtil.generateToken(existingUser.getUsername());

            return ResponseEntity.ok(Map.of(
                    "message", "Login successful",
                    "username", existingUser.getUsername(),
                    "token", token
                    ));

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred during login"));
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserbyEmail(@PathVariable String email) {
        Optional<UserModel> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            return ResponseEntity
                    .status(404)
                    .body("User not found");
        }

        return ResponseEntity.ok(user.get());
    }

    // get by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body(null));
    }

    // get all
    @GetMapping
    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    // update user
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserModel updatedUser) {
        Optional<UserModel> existingUserOpt = userRepository.findById(id);

        if (!existingUserOpt.isPresent()) {
            return ResponseEntity
                    .status(404)
                    .body("User not found");
        }

        UserModel existingUser = existingUserOpt.get();
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setRole(updatedUser.getRole());

        userRepository.save(existingUser);
        return ResponseEntity.ok(existingUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        Optional<UserModel> existingUserOpt = userRepository.findById(id);
        if (!existingUserOpt.isPresent()) {
            return ResponseEntity
                    .status(404)
                    .body("User not found");
        }

        userRepository.delete(existingUserOpt.get());
        return ResponseEntity.ok("User deleted successfully");

    }
}
