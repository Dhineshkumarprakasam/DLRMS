package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "personnel_credential")
public class PersonnelCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personnel_id", nullable = false, unique = true)
    private User personnel;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    public Long getId() {
        return id;
    }

    public User getPersonnel() {
        return personnel;
    }

    public void setPersonnel(User personnel) {
        this.personnel = personnel;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}