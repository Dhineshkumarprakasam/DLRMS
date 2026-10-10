package com.example.vehicle.controller;

import com.example.vehicle.model.VehicleAssignment;
import com.example.vehicle.repository.VehicleAssignmentRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/vehicle-assignments")
public class VehicleAssignmentController {

    private final VehicleAssignmentRepository vehicleAssignmentRepository;

    public VehicleAssignmentController(VehicleAssignmentRepository vehicleAssignmentRepository) {
        this.vehicleAssignmentRepository = vehicleAssignmentRepository;
    }

    @GetMapping
    public List<VehicleAssignment> getAllAssignments() {
        return vehicleAssignmentRepository.findAll();
    }

    @GetMapping("/{id}")
    public VehicleAssignment getAssignmentById(@PathVariable Long id) {
        return vehicleAssignmentRepository.findById(id).orElse(null);
    }

    @PostMapping("/save")
    public VehicleAssignment saveAssignment(@RequestBody VehicleAssignment assignment) {
        return vehicleAssignmentRepository.save(assignment);
    }

    @PutMapping("/{id}")
    public void updateAssignment(@PathVariable Long id,
                                 @RequestBody VehicleAssignment assignment) {
        assignment.setId(id);
        vehicleAssignmentRepository.save(assignment);
    }

    @DeleteMapping("/{id}")
    public void deleteAssignment(@PathVariable Long id) {
        vehicleAssignmentRepository.deleteById(id);
    }
}