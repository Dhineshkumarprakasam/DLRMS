package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.demo.model.*;

import java.util.Optional;

@Repository
public interface PersonnelRepository extends JpaRepository<User, Long> {

	Optional<User> findByServiceNumber(String serviceNumber);
}