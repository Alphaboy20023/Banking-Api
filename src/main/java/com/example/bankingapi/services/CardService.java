package com.example.bankingapi.services;

import com.example.bankingapi.models.AccountModel;
import com.example.bankingapi.models.CardModel;
import com.example.bankingapi.Repositories.CardRepository;
import com.example.bankingapi.Repositories.AccountRepository;
import com.example.bankingapi.models.TransactionModel;
import com.example.bankingapi.Repositories.TransactionRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public CardModel createCardForAccount(AccountModel account) {
        // Generate random card number and CVV
        String cardNumber = generateCardNumber();
        String cvv = generateCvv();

        // Set expiry date = 4 years from now
        LocalDateTime expiryDate = LocalDateTime.now().plusYears(4);

        CardModel card = new CardModel();
        card.setCardNumber(cardNumber);
        card.setCvv(cvv);
        card.setExpiryDate(expiryDate);

        // link card to account
        card.setAccount(account);

        // Link account back to card
        account.setCard(card);

        // Save card
        return cardRepository.save(card);
    }

    @Transactional
    public Map<String, Object> withdrawWithCard(String accountNumber, String cardNumber, String cvv, String pin,
            BigDecimal amount) {
        AccountModel account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        CardModel card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        if (!card.getAccount().getAccountNumber().equals(accountNumber)) {
            throw new RuntimeException("This card does not belong to the provided account");
        }

        if (!card.getCvv().equals(cvv)) {
            throw new RuntimeException("Invalid CVV");
        }

        if (card.getPin() == null || !card.getPin().equals(pin)) {
            throw new RuntimeException("Invalid card PIN");
        }

        if (!card.isActive()) {
            throw new RuntimeException("Card not active");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // Record transaction
        TransactionModel transaction = new TransactionModel();
        transaction.setFromAccount(account);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionModel.TransactionType.CARD_WITHDRAWAL);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Withdrawal successful");
        response.put("remaining Balance", account.getBalance());
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    private String generateCardNumber() {
        long num = 4000000000000000L + (long) (Math.random() * 100000000000000L);
        return String.valueOf(num).substring(0, 16);
    }

    private String generateCvv() {
        return String.valueOf((int) (100 + Math.random() * 900));
    }
}