package com.example.demo.controller;

import com.example.demo.model.LogisticsRequest;
import com.example.demo.model.MaintenanceRecord;
import com.example.demo.model.ResourceAllocation;
import com.example.demo.repository.LogisticsRequestRepository;
import com.example.demo.repository.MaintenanceRecordRepository;
import com.example.demo.repository.ResourceAllocationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LogisticsController {

    private final LogisticsRequestRepository requestRepository;
    private final MaintenanceRecordRepository maintenanceRepository;
    private final ResourceAllocationRepository allocationRepository;

    public LogisticsController(LogisticsRequestRepository requestRepository,
                               MaintenanceRecordRepository maintenanceRepository,
                               ResourceAllocationRepository allocationRepository) {
        this.requestRepository = requestRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.allocationRepository = allocationRepository;
    }

    @GetMapping("/requests")
    public List<LogisticsRequest> getRequests() {
        return requestRepository.findAll();
    }

    @PostMapping("/requests")
    public LogisticsRequest createRequest(@RequestBody LogisticsRequest request) {
        request.setId(null);
        request.setStatus("PENDING");
        request.setAssignedTo(null);
        request.setAdminNote(null);
        return requestRepository.save(request);
    }

    @PutMapping("/requests/{id}")
    public LogisticsRequest updateRequest(@PathVariable Long id, @RequestBody LogisticsRequest update) {
        LogisticsRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found."));
        request.setStatus(update.getStatus());
        request.setAssignedTo(update.getAssignedTo());
        request.setAdminNote(update.getAdminNote());
        return requestRepository.save(request);
    }

    @DeleteMapping("/requests/{id}")
    public void deleteRequest(@PathVariable Long id) {
        requestRepository.deleteById(id);
    }

    @GetMapping("/maintenance")
    public List<MaintenanceRecord> getMaintenance() {
        return maintenanceRepository.findAll();
    }

    @PostMapping("/maintenance")
    public MaintenanceRecord createMaintenance(@RequestBody MaintenanceRecord record) {
        record.setId(null);
        return maintenanceRepository.save(record);
    }

    @PutMapping("/maintenance/{id}")
    public MaintenanceRecord updateMaintenance(@PathVariable Long id, @RequestBody MaintenanceRecord update) {
        update.setId(id);
        return maintenanceRepository.save(update);
    }

    @DeleteMapping("/maintenance/{id}")
    public void deleteMaintenance(@PathVariable Long id) {
        maintenanceRepository.deleteById(id);
    }

    @GetMapping("/allocations")
    public List<ResourceAllocation> getAllocations() {
        return allocationRepository.findAll();
    }

    @PostMapping("/allocations")
    public ResourceAllocation createAllocation(@RequestBody ResourceAllocation allocation) {
        allocation.setId(null);
        return allocationRepository.save(allocation);
    }

    @PutMapping("/allocations/{id}")
    public ResourceAllocation updateAllocation(@PathVariable Long id, @RequestBody ResourceAllocation update) {
        update.setId(id);
        return allocationRepository.save(update);
    }

    @DeleteMapping("/allocations/{id}")
    public void deleteAllocation(@PathVariable Long id) {
        allocationRepository.deleteById(id);
    }

}
