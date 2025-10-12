package com.example.bankingapi.Repositories;

import com.example.bankingapi.models.AccountModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface AccountRepository extends JpaRepository<AccountModel, Long> {
    
    Optional<AccountModel> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    
}
