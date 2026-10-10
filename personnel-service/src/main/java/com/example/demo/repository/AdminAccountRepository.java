package com.example.demo.repository;

import com.example.demo.model.AdminAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminAccountRepository extends JpaRepository<AdminAccount, Long> {

    Optional<AdminAccount> findByForceNumber(String forceNumber);

    boolean existsByForceNumber(String forceNumber);
}