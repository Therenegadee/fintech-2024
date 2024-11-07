package ru.tbank.hw12.v1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.tbank.hw12.dto.LoginRequest;
import ru.tbank.hw12.dto.LoginResponse;
import ru.tbank.hw12.dto.ResetPasswordRequest;
import ru.tbank.hw12.dto.SignupRequest;
import ru.tbank.hw12.dto.SignupResponse;
import ru.tbank.hw12.entity.Role;
import ru.tbank.hw12.entity.Token;
import ru.tbank.hw12.entity.User;
import ru.tbank.hw12.entity.enums.RoleEnum;
import ru.tbank.hw12.exception.InternalServerErrorException;
import ru.tbank.hw12.exception.NotFoundException;
import ru.tbank.hw12.repository.RoleRepository;
import ru.tbank.hw12.repository.TokenRepository;
import ru.tbank.hw12.repository.UserRepository;
import ru.tbank.hw5.exception.BadRequestException;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private final TokenRepository tokenRepository;

    public SignupResponse signup(SignupRequest request) {
        Role userRole = roleRepository.findRoleByName(RoleEnum.USER)
                .orElseThrow(() -> new InternalServerErrorException("Стандартная роль " + RoleEnum.USER + " не была найдена в Базе Даных."));

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return SignupResponse.builder()
                .userId(user.getId())
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .roles(user.getRoles().stream().map(Role::getName).toList())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Пользователь с именем " + request.getUsername() + " не был найден!"));

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        String jwtToken = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();
    }

    public ResponseEntity<?> resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("Пользователь с именем " + request.getUsername() + " не был найден!"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Пароль был успешно обновлен!");
    }

    public void logout(String token) {
        token = token.substring(7);
        String username = jwtService.extractUsername(token);

        if (!jwtService.isTokenValid(token, userService.loadUserByUsername(username))) {
            throw new BadRequestException("Переданный токен не валидный!");
        }

        Token invalidatedToken = Token.builder()
                .token(token)
                .isBlackListed(true)
                .build();

        tokenRepository.save(invalidatedToken);
    }
}
