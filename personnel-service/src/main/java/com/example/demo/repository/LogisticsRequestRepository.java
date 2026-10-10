package com.example.demo.repository;

import com.example.demo.model.LogisticsRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogisticsRequestRepository extends JpaRepository<LogisticsRequest, Long> {
}
