package com.example.bankingapi.models;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
@Table(name = "cards")
public class CardModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber; // auto-generate

    @Column(nullable = true)
    private String cvv; // auto-generate

    @Column(nullable = true)
    private String pin; // same as account pin or null until set

    private boolean isActive = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    private AccountModel account;

    public CardModel() {}

    public CardModel(String cardNumber, AccountModel account) {
        this.cardNumber = cardNumber;
        this.account = account;
        
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plus(4, ChronoUnit.YEARS); // 4 years expiry
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getCardNumber() { return cardNumber; }
    public String getCvv() { return cvv; }
    public String getPin() { return pin; }
    public boolean getIsActive() { return isActive; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public AccountModel getAccount() { return account; }

    public void setAccount(AccountModel account) { this.account = account; }
    // make card active once pin is set
    public void setPin(String pin) { this.pin = pin; this.isActive = true;  }
    public void setCardNumber(String cardNumber) { this.cardNumber=cardNumber; }
    public void setCvv(String cvv) { this.cvv = cvv; }
    public void  setExpiryDate( LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public boolean isActive() { return isActive; }


}
