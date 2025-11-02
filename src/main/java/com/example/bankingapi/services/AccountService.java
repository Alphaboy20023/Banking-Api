package com.example.bankingapi.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.bankingapi.Repositories.AccountRepository;
import com.example.bankingapi.Repositories.CardRepository;
import com.example.bankingapi.Repositories.TransactionRepository;

import com.example.bankingapi.models.AccountModel;
import com.example.bankingapi.models.CardModel;
import com.example.bankingapi.models.TransactionModel;
import com.example.bankingapi.services.EmailService;

// ALL ACCOUNT CRUD
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private EmailService emailService;

    @Autowired
    private TransactionRepository transactionRepository;

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

        // Sender
        AccountModel fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        // Receiver
        AccountModel toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

        BigDecimal totalSentToday = transactionRepository.findTotalSentToday(fromAccount.getId(), startOfDay, endOfDay);
        // if no transactions, think the total is zero
        if (totalSentToday == null)
            totalSentToday = BigDecimal.ZERO;

        BigDecimal projectedTotal = totalSentToday.add(amount);

        // if total amount is greater than 0, throw the error, i.e 10,001 -> error
        if (projectedTotal.compareTo(fromAccount.getDailyLimit()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Daily transaction limit of ₦ " + fromAccount.getDailyLimit() + " exceeded.");
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        TransactionModel transaction = transactionService.recordTransfer(fromAccount, toAccount, amount);
        String transactionId = transaction.getTransactionId();

        // format i.e 2000 -> 2,000
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "NG"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);

        String formattedAmount = nf.format(amount);
        String formattedSenderBalance = nf.format(fromAccount.getBalance());
        String formattedReceiverBalance = nf.format(toAccount.getBalance());

        try {
            // Sender email
            Map<String, Object> senderData = Map.of(
                    "name", fromAccount.getUser().getUsername(),
                    "transactionType", "sent",
                    "direction", "from",
                    "amount", formattedAmount,
                    // "role","sender",
                    "transactionId", transactionId,
                    "date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "balance", formattedSenderBalance);

            emailService.sendHtmlMail(
                    fromAccount.getUser().getEmail(),
                    "Debit Alert - ₦" + formattedAmount,
                    "transaction-success",
                    senderData);

            // Receiver email
            Map<String, Object> receiverData = Map.of(
                    "name", toAccount.getUser().getUsername(),
                    "transactionType", "received",
                    "direction", "into",
                    "amount", formattedAmount,
                    "role", "Sender",
                    "getSenderName", fromAccount.getUser().getUsername(),
                    "transactionId", transactionId,
                    "date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    "balance", formattedReceiverBalance);

            emailService.sendHtmlMail(
                    toAccount.getUser().getEmail(),
                    "Credit Alert - ₦" + formattedAmount,
                    "transaction-success",
                    receiverData);
        } catch (Exception e) {
            System.err.println("Transaction completed, but email failed: " + e.getMessage());
        }

    }

    // Pin
    public void setPin(String accountNumber, String pin) {
        AccountModel account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (pin == null || pin.length() < 4 || pin.length() > 6) {
            throw new RuntimeException("PIN must be between 4 and 6 digits");
        }

        // hash pin
        account.setPin(passwordEncoder.encode(pin));
        accountRepository.save(account);

        // set account pin same as card pin
        if (account.getCard() != null) {
            CardModel card = account.getCard();
            card.setPin(pin); // same pin for both
            cardRepository.save(card);
        }
    }

    public boolean validatePin(String accountNumber, String inputPin) {
        AccountModel account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Compare entered PIN with stored PIN
        return passwordEncoder.matches(inputPin, account.getPin());
    }

    public String getUsernameByAccountNumber(String accountNumber) {
        AccountModel account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found for number: " + accountNumber));
        return account.getUser().getUsername();
    }

    

}
