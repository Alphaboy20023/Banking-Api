package com.example.bankingapi.models;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
// import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "accounts")
public class AccountModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @NotBlank
    private String accountNumber;

    @Column
    @Size(min = 4)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pin;

    @Column(nullable = false)
    @PositiveOrZero
    private BigDecimal balance;

    @Column(nullable = true, precision = 19, scale = 2)
    // precision here is saying the max amount of money, i.e the standard, up to 19
    // digits,
    // scale means decimal after, i.e 2
    @PositiveOrZero
    private BigDecimal dailyLimit = new BigDecimal("10000.00");

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

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonManagedReference
    private CardModel card;

    // @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    // private List<TransactionModel> transactions;

    // contstructors
    public AccountModel() {
    }


    public CardModel getCard() {
        return card;
    }

    public AccountModel(String accountNumber, AccountType accountType, UserModel user, String pin) {
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.user = user;
        setPin(pin);
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

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public String getPin() {
        return pin;
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
    // return transactions;
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

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void setUser(UserModel user) {
        this.user = user;
        if (user != null && user.getAccount() != this) {
            user.setAccount(this);
        }
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setCard(CardModel card) {
        this.card = card;
    }

    // public void setTransactions(List<TransactionModel> transactions) {
    // this.transactions = transactions;
    // }

}
