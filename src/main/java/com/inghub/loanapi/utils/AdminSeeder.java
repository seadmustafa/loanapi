package com.inghub.loanapi.utils;

import com.inghub.loanapi.entity.Role;
import com.inghub.loanapi.entity.User;
import com.inghub.loanapi.enums.RoleEnum;
import com.inghub.loanapi.repository.RoleRepository;
import com.inghub.loanapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@Order(2)
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;


    public AdminSeeder(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createSuperAdministratorAndUser();
    }

    private void createSuperAdministratorAndUser() {
        createUserWithRole("Super Admin", "a@a.com", "1", RoleEnum.ADMIN);
        createUserWithRole("Regular Customer", "u@a.com", "1", RoleEnum.CUSTOMER);
    }

    private void createUserWithRole(String fullName, String email, String password, RoleEnum roleEnum) {
        Optional<Role> optionalRole = roleRepository.findByName(roleEnum);
        if (optionalRole.isEmpty()) {
            log.error("Role {} not found. User creation aborted.", roleEnum);
            return;
        }

        User user = new User()
                .setFullName(fullName)
                .setEmail(email)
                .setPassword(passwordEncoder.encode(password))
                .setRole(optionalRole.get());

        userRepository.save(user);
        log.info("Created {} user with email: {}", roleEnum, email);
    }

}
