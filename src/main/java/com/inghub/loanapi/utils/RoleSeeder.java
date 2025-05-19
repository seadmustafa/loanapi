package com.inghub.loanapi.utils;


import com.inghub.loanapi.entity.Role;
import com.inghub.loanapi.enums.RoleEnum;
import com.inghub.loanapi.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@Order(1)
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;


    public RoleSeeder(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadRoles();
    }

    private void loadRoles() {
        RoleEnum[] roleNames = new RoleEnum[]{RoleEnum.CUSTOMER, RoleEnum.ADMIN};
        Map<RoleEnum, String> roleDescriptionMap = Map.of(
                RoleEnum.CUSTOMER, "Default customer role",
                RoleEnum.ADMIN, "Administrator role"
         );

        Arrays.stream(roleNames).forEach((roleName) -> {
            Optional<Role> optionalRole = roleRepository.findByName(roleName);

            optionalRole.ifPresentOrElse(System.out::println, () -> {
                Role roleToCreate = new Role();

                roleToCreate.setName(roleName)
                        .setDescription(roleDescriptionMap.get(roleName));

                roleRepository.save(roleToCreate);
                log.info("Created role {}", roleToCreate);
            });
        });
    }
}
