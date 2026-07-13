package com.coworking.reservation.config;

import com.coworking.reservation.entity.UserAccount;
import com.coworking.reservation.entity.enums.Role;
import com.coworking.reservation.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevDataInitializer implements ApplicationRunner{

    private static final String ADMIN_EMAIL = "admin@coworking.com";
    private static final String ADMIN_PASSWORD = "Admin12345!";

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userAccountRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }

        UserAccount admin = new UserAccount(
                "System Admin",
                ADMIN_EMAIL,
                passwordEncoder.encode(ADMIN_PASSWORD),
                Role.ADMIN
        );

        userAccountRepository.save(admin);
    }
}
