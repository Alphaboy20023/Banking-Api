package com.example.bankingapi.dto;

public class SetPinRequest {
    private String accountNumber;
    private String pin;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setPin(String Pin) {
        this.pin = Pin;
    }


}
