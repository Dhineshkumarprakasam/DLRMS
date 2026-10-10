package com.example.vehicle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.vehicle.model.VehicleAssignment;

@Repository
public interface VehicleAssignmentRepository extends JpaRepository<VehicleAssignment, Long> {
}