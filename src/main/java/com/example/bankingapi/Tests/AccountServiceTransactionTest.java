// package com.example.bankingapi.Tests;

// import static org.junit.jupiter.api.Assertions.assertEquals;

// import java.math.BigDecimal;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.beans.factory.annotation.Autowired;

// import com.example.bankingapi.Repositories.AccountRepository;
// import com.example.bankingapi.models.AccountModel;
// import com.example.bankingapi.services.AccountService;

// @SpringBootTest
// @ActiveProfiles("test") // Use a test database if you have one configured
// public class AccountServiceTransactionTest {

//     @Autowired
//     private AccountService accountService;

//     @Autowired
//     private AccountRepository accountRepository;

//     @Test
//     public void testTransferRollbackOnFailure() {
//         // 1. SETUP: Create two accounts with known balances
//         AccountModel sender = new AccountModel();
//         sender.setAccountNumber("111");
//         sender.setBalance(new BigDecimal("1000.00"));
//         accountRepository.save(sender);

//         AccountModel receiver = new AccountModel();
//         receiver.setAccountNumber("222");
//         receiver.setBalance(new BigDecimal("500.00"));
//         accountRepository.save(receiver);

//         // 2. Try to transfer, but we expect an error
//         //  force an error by sending an invalid amount or mocking a service failure
//         try {
//             // Passing a null amount usually triggers a NullPointerException in the logic
//             accountService.transfer("111", "222", null); 
//         } catch (Exception e) {
//             System.out.println("Caught expected error: " + e.getMessage());
//         }

//         // 3. VERIFY: Check if the money stayed the same
//         AccountModel savedSender = accountRepository.findByAccountNumber("111").get();
//         AccountModel savedReceiver = accountRepository.findByAccountNumber("222").get();

//         // If @Transactional works, the sender should STILL have 1000.00
//         assertEquals(new BigDecimal("1000.00"), savedSender.getBalance());
//         assertEquals(new BigDecimal("500.00"), savedReceiver.getBalance());
        
//         System.out.println("✅ Rollback successful! Money didn't move because of the error.");
//     }
// }