package com.example.demo.config;

import com.example.demo.model.AdminAccount;
import com.example.demo.repository.AdminAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class AdminProvisioningCommand implements CommandLineRunner {

    private static final System.Logger LOGGER = System.getLogger(AdminProvisioningCommand.class.getName());

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public AdminProvisioningCommand(AdminAccountRepository adminAccountRepository,
                                    PasswordEncoder passwordEncoder,
                                    Environment environment) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        if (Arrays.stream(args).noneMatch("--provision-admin"::equals)) {
            return;
        }

        String forceNumber = environment.getProperty("DLRMS_ADMIN_FORCE_NUMBER");
        String password = environment.getProperty("DLRMS_ADMIN_PASSWORD");
        if (forceNumber == null || forceNumber.isBlank() || password == null || password.length() < 12) {
            throw new IllegalStateException(
                    "Admin provisioning requires DLRMS_ADMIN_FORCE_NUMBER and a DLRMS_ADMIN_PASSWORD of at least 12 characters.");
        }

        String normalizedForceNumber = forceNumber.trim();
        if (adminAccountRepository.existsByForceNumber(normalizedForceNumber)) {
            throw new IllegalStateException("An Admin account already exists for that force number.");
        }

        AdminAccount account = new AdminAccount();
        account.setForceNumber(normalizedForceNumber);
        account.setPasswordHash(passwordEncoder.encode(password));
        adminAccountRepository.save(account);
        LOGGER.log(System.Logger.Level.INFO, "Admin account provisioned.");
    }
}