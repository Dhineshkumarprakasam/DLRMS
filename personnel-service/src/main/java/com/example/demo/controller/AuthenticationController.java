package com.example.demo.controller;

import com.example.demo.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/authenticate")
    public AuthenticationService.AuthenticationResult authenticate(@RequestBody(required = false) LoginRequest request) {
        if (request == null || isBlank(request.forceNumber()) || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Force number and password are required.");
        }
        return authenticationService.authenticate(request.forceNumber(), request.password());
    }

    @PostMapping("/personnel/{forceNumber}/credentials")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void setPersonnelPassword(@PathVariable String forceNumber,
                                     @RequestBody(required = false) CredentialRequest request) {
        if (request == null || isBlank(request.password())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
        }
        authenticationService.setPersonnelPassword(forceNumber, request.password());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record LoginRequest(String forceNumber, String password) {}

    public record CredentialRequest(String password) {}
}