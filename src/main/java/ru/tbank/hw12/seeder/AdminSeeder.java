package ru.tbank.hw12.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.tbank.hw12.dto.SignupRequest;
import ru.tbank.hw12.entity.Role;
import ru.tbank.hw12.entity.User;
import ru.tbank.hw12.entity.enums.RoleEnum;
import ru.tbank.hw12.repository.RoleRepository;
import ru.tbank.hw12.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationListener<ContextRefreshedEvent> {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        this.createAdmin();
    }

    private void createAdmin() {
        SignupRequest request = SignupRequest.builder()
                .username("admin")
                .email("admin")
                .password("admin")
                .build();

        Optional<Role> optionalRole = roleRepository.findRoleByName(RoleEnum.ADMIN);
        Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());

        if (optionalRole.isEmpty() || optionalUser.isPresent()) {
            return;
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(optionalRole.get()))
                .build();

        userRepository.save(user);
    }
}
