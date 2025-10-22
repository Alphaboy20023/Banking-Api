package com.example.bankingapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.bankingapi.models.CardModel;
import com.example.bankingapi.models.UserModel;
import com.example.bankingapi.Repositories.CardRepository;
import com.example.bankingapi.Repositories.UserRepository;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/card-info/{id}")
    public ResponseEntity<?> getCardInfo(@PathVariable Long id) {
        try {
            Optional<UserModel> userOpt = userRepository.findById(id);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            UserModel user = userOpt.get();
            if (user.getAccount() == null || user.getAccount().getCard() == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Card not found"));
            }

            CardModel card = user.getAccount().getCard();

            return ResponseEntity.ok(Map.of(
                    "cardNumber", card.getCardNumber(),
                    "expiryDate", card.getExpiryDate(),
                    "cvv", card.getCvv(),
                    "hasPin", card.getPin() != null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all-cards")
    public ResponseEntity<?> getAllCards(@RequestHeader("Authorization") String token, Principal principal) {

        UserModel currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole().name())) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Access denied");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(cardRepository.findAll());
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(@PathVariable Long cardId, Principal principal) {

        UserModel currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole().name())) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Access denied");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Optional<CardModel> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Card not found"));
        }

        cardRepository.deleteById(cardId);
        return ResponseEntity.ok(Map.of("message", "Card deleted successfully"));
    }

}

// {
// "cardNumber": "4000123412341234",
// "cvv": "123",
// "pin": "4321",
// "amount": 5000
// }
