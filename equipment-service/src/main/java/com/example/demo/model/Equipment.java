package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "serial_number", unique = true, length = 50, nullable = false)
    private String serialNumber;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(length = 50)
    private String status;

    @Column(name = "operational_condition", length = 100)
    private String operationalCondition;

    @Column(length = 100)
    private String location;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(length = 100)
    private String ownership;

    public Equipment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOperationalCondition() { return operationalCondition; }
    public void setOperationalCondition(String operationalCondition) { this.operationalCondition = operationalCondition; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public String getOwnership() { return ownership; }
    public void setOwnership(String ownership) { this.ownership = ownership; }
}