package ru.tbank.hw12.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.tbank.hw12.entity.Role;
import ru.tbank.hw12.entity.enums.RoleEnum;
import ru.tbank.hw12.repository.RoleRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Order(Integer.MIN_VALUE)
public class RoleSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.loadRoles();
    }

    private void loadRoles() {
        Map<RoleEnum, String> roleDescriptionMap = Map.of(
                RoleEnum.USER, "Default user role",
                RoleEnum.ADMIN, "Administrator role"
        );

        Set<RoleEnum> rolesInDb = roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());


        Arrays.stream(RoleEnum.values())
                .filter(roleName -> !rolesInDb.contains(roleName))
                .forEach(roleName -> {

                    Role roleToCreate = Role.builder()
                            .name(roleName)
                            .description(roleDescriptionMap.get(roleName))
                            .createdAt(LocalDate.now())
                            .updatedAt(LocalDate.now())
                            .build();
                    roleRepository.save(roleToCreate);
                });
    }
}
