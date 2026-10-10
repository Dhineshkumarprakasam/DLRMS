package com.example.vehicle.controller;

import com.example.vehicle.model.Driver;
import com.example.vehicle.repository.DriverRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverRepository driverRepository;

    public DriverController(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @GetMapping
    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    @GetMapping("/{id}")
    public Driver getDriverById(@PathVariable Long id) {
        return driverRepository.findById(id).orElse(null);
    }

    @PostMapping("/save")
    public Driver saveDriver(@RequestBody Driver driver) {
        return driverRepository.save(driver);
    }

    @PutMapping("/{id}")
    public void updateDriver(@PathVariable Long id, @RequestBody Driver driver) {
        driver.setId(id);
        driverRepository.save(driver);
    }

    @DeleteMapping("/{id}")
    public void deleteDriver(@PathVariable Long id) {
        driverRepository.deleteById(id);
    }
}