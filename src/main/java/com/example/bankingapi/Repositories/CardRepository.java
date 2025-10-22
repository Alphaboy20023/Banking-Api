package com.example.bankingapi.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bankingapi.models.CardModel;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CardModel, Long> {

    boolean existsByCardNumber(String cardNumber);

    Optional<CardModel> findByAccount_Id(Long accountId);

    Optional<CardModel> findByCardNumber(String cardNumber);
    
}
