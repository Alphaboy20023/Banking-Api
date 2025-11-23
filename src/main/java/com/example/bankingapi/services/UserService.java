package com.example.bankingapi.services;

import org.springframework.stereotype.Service;

@Service
public class UserService {
 
    
    public String generateBankVerificationNumber() {
        long num = 4000000000000000L + (long) (Math.random() * 100000000000000L);
        return String.valueOf(num).substring(0, 12);
    }
}
