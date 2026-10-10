package com.example.demo.repository;

import com.example.demo.model.PersonnelCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonnelCredentialRepository extends JpaRepository<PersonnelCredential, Long> {

    Optional<PersonnelCredential> findByPersonnel_Id(Long personnelId);
}