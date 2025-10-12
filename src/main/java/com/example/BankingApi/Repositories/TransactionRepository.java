package com.example.BankingApi.Repositories;

import com.example.BankingApi.models.AccountModel;
import com.example.BankingApi.models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Long>{
    // Fetch all transactions where the account is either the sender or receiver
    List<TransactionModel> findByFromAccountOrToAccount(AccountModel fromAccount, AccountModel toAccount);

    // Fetch all transactions involving a specific account ID (custom query)
    List<TransactionModel> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId);
}
