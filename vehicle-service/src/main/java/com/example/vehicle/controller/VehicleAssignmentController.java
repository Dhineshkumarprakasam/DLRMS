package com.example.vehicle.controller;

import com.example.vehicle.model.VehicleAssignment;
import com.example.vehicle.repository.VehicleAssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-assignments")
public class VehicleAssignmentController {

    private final VehicleAssignmentRepository assignmentRepository;

    public VehicleAssignmentController(VehicleAssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleAssignment> getAssignmentById(@PathVariable Long id) {
        return assignmentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/save")
    public ResponseEntity<VehicleAssignment> saveAssignment(
            @RequestBody VehicleAssignment assignment) {
        VehicleAssignment savedAssignment = assignmentRepository.save(assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAssignment);
    }
    @GetMapping
    public ResponseEntity<List<VehicleAssignment>> getAllAssignments() {
        return ResponseEntity.ok(assignmentRepository.findAll());
    }
    @PutMapping("/{id}")
    public ResponseEntity<VehicleAssignment> updateAssignment(
            @PathVariable Long id,
            @RequestBody VehicleAssignment assignment) {

        return assignmentRepository.findById(id)
                .map(existingAssignment -> {
                    assignment.setId(existingAssignment.getId());
                    return ResponseEntity.ok(assignmentRepository.save(assignment));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        if (!assignmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        assignmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}