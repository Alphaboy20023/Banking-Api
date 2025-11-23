package com.example.bankingapi.models;

import jakarta.persistence.*;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    @Email
    private String email;

    @Column(nullable = false)
    @NotBlank
    @Size(min = 8)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column
    private String username;

    @Column(nullable = true, unique = true)
    private String bankVerificationNumber;

    
    public enum Role {
        ADMIN,
        SUPERUSER,
        USER
    }
    
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private boolean isActive = false;
    
    private boolean isVerified = false;

    // Constructors
    public UserModel() {}

    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private AccountModel account;

    public UserModel(String email, String password, String username, Role role) {
        this.email = email;
        setPassword(password);
        this.username = username;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // getters
    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public AccountModel getAccount() { return account; }

    public String getBankVerificationNumber() {
        return bankVerificationNumber;
    }

    // setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public void setAccount(AccountModel account) {
        this.account = account;
        if (account != null && account.getUser() != this) {
            account.setUser(this);
        }
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setBankVerificationNumber(String bankVerificationNumber) {
        this.bankVerificationNumber = bankVerificationNumber;
    }


}
