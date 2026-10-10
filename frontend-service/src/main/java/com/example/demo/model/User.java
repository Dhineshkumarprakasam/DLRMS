package com.example.demo.model;

import java.time.LocalDate;

public class User {

    private Long id;
    private String serviceNumber;
    private String name;
    private LocalDate dateOfBirth;
    private String rank;
    private String role;
    private Long unitId;
    private String currentLocation;
    private String status = "AVAILABLE";

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceNumber() { return serviceNumber; }
    public void setServiceNumber(String serviceNumber) { this.serviceNumber = serviceNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { this.currentLocation = currentLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
