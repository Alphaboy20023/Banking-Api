package com.example.bankingapi.models;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
// import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name  = "accounts")
public class AccountModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String accountNumber;

    @Column(nullable = false)
    @PositiveOrZero
    private BigDecimal balance;

    public enum AccountType {
        SAVINGS,
        CURRENT,
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType = AccountType.SAVINGS;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // relations
    // one account -> one user
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private UserModel user;

    // @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    // private List<TransactionModel> transactions;

    // contstructors
    public AccountModel() {}

    public AccountModel(String accountNumber, AccountType accountType, UserModel user) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.user = user;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // getters
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public UserModel getUser() {
        return user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // public List<TransactionModel> getTransactions() {
    //     return transactions;
    // }

    // setters
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setUser(UserModel user) {
        this.user = user;
        if (user != null && user.getAccount() != this) {
            user.setAccount(this);
        }
    }

    // public void setTransactions(List<TransactionModel> transactions) {
    //     this.transactions = transactions;
    // }

}
