package com.example.bankingapi.Repositories;

import com.example.bankingapi.models.AccountModel;
import com.example.bankingapi.models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionModel, Long> {
    // Fetch all transactions where the account is either the sender or receiver
    List<TransactionModel> findByFromAccountOrToAccount(AccountModel fromAccount, AccountModel toAccount);

    // Query sums all transfers sent today from a given account
    @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM TransactionModel t
                WHERE t.fromAccount.id = :accountId
                  AND t.createdAt BETWEEN :startOfDay AND :endOfDay
            """)

    BigDecimal findTotalSentToday(
            @Param("accountId") Long accountId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    // Fetch all transactions involving a specific account ID (custom query)
    List<TransactionModel> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId);

    Optional<TransactionModel> findByTransactionId(String transactionId);
}
