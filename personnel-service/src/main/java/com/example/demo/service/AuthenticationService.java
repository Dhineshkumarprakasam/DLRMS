package com.example.demo.service;

import com.example.demo.model.AdminAccount;
import com.example.demo.model.PersonnelCredential;
import com.example.demo.model.User;
import com.example.demo.repository.AdminAccountRepository;
import com.example.demo.repository.PersonnelCredentialRepository;
import com.example.demo.repository.PersonnelRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS = "Invalid force number or password.";
    private static final int MINIMUM_PASSWORD_LENGTH = 12;

    private final AdminAccountRepository adminAccountRepository;
    private final PersonnelRepository personnelRepository;
    private final PersonnelCredentialRepository personnelCredentialRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(AdminAccountRepository adminAccountRepository,
                                 PersonnelRepository personnelRepository,
                                 PersonnelCredentialRepository personnelCredentialRepository,
                                 PasswordEncoder passwordEncoder) {
        this.adminAccountRepository = adminAccountRepository;
        this.personnelRepository = personnelRepository;
        this.personnelCredentialRepository = personnelCredentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AuthenticationResult authenticate(String forceNumber, String password) {
        if (forceNumber == null || forceNumber.isBlank() || password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Force number and password are required.");
        }

        String normalizedForceNumber = forceNumber.trim();
        AdminAccount admin = adminAccountRepository.findByForceNumber(normalizedForceNumber).orElse(null);
        if (admin != null) {
            if (passwordEncoder.matches(password, admin.getPasswordHash())) {
                return new AuthenticationResult(normalizedForceNumber, "ADMIN");
            }
            throw invalidCredentials();
        }

        User personnel = personnelRepository.findByServiceNumber(normalizedForceNumber).orElse(null);
        if (personnel == null) {
            throw invalidCredentials();
        }

        PersonnelCredential credential = personnelCredentialRepository
                .findByPersonnel_Id(personnel.getId()).orElse(null);
        if (credential == null || !passwordEncoder.matches(password, credential.getPasswordHash())) {
            throw invalidCredentials();
        }

        return new AuthenticationResult(normalizedForceNumber, "USER");
    }

    @Transactional
    public void setPersonnelPassword(String forceNumber, String password) {
        if (password == null || password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain at least 12 characters.");
        }

        User personnel = personnelRepository.findByServiceNumber(forceNumber.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Personnel record not found."));
        PersonnelCredential credential = personnelCredentialRepository.findByPersonnel_Id(personnel.getId())
                .orElseGet(PersonnelCredential::new);
        credential.setPersonnel(personnel);
        credential.setPasswordHash(passwordEncoder.encode(password));
        personnelCredentialRepository.save(credential);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
    }

    public record AuthenticationResult(String forceNumber, String role) {}
}