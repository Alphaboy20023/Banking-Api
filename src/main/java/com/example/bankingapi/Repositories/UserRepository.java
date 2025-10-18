package com.example.bankingapi.Repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.bankingapi.models.UserModel;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
        // find user by email
        Optional<UserModel> findByEmail(String email);

        Optional<UserModel> findByUsername(String username);

        // Check if a user with this email exists
        boolean existsByEmail(String email);
}
