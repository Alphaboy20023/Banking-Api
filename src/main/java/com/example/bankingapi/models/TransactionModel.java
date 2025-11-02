package com.example.bankingapi.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "transactions")
public class TransactionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER,
        DEBIT,
        CREDIT,
        CARD_WITHDRAWAL
    };

    @NotNull(message = "Transaction type is required")
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable = true, unique = true)
    private String transactionId; // auto genrate after saving transaction

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // many transactions to an account
    // sender
    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private AccountModel fromAccount;

    // receiver
    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private AccountModel toAccount;

    // constructor
    public TransactionModel() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

     public Long getId() { return id; }

    public AccountModel getFromAccount() { return fromAccount; }
    public AccountModel getToAccount() { return toAccount; }

    public TransactionType getTransactionType() { return transactionType; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return createdAt; }
    public String getTransactionId() { return transactionId; }

    // setters
    public void setFromAccount(AccountModel fromAccount) { this.fromAccount = fromAccount; }
    public void setToAccount(AccountModel toAccount) { this.toAccount = toAccount; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setTimestamp(LocalDateTime timestamp) { this.createdAt = timestamp; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
}
