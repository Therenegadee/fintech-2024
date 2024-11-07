package ru.tbank.hw12.v1.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.tbank.hw10.dto.EventDto;
import ru.tbank.hw12.dto.LoginRequest;
import ru.tbank.hw12.dto.LoginResponse;
import ru.tbank.hw12.dto.SignupRequest;
import ru.tbank.hw12.dto.SignupResponse;
import ru.tbank.hw12.entity.Role;
import ru.tbank.hw12.entity.User;
import ru.tbank.hw12.entity.enums.RoleEnum;
import ru.tbank.hw12.repository.RoleRepository;
import ru.tbank.hw12.repository.UserRepository;
import ru.tbank.hw12.v1.service.AuthService;
import ru.tbank.hw12.v1.service.JwtService;
import ru.tbank.hw12.v1.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ApplicationContext context;


    private static final String MOCK_USERNAME = "user";
    private static final String MOCK_PASSWORD = "userPASSWORD";

    private static final String ADMIN_USERNAME = "adminchik";
    private static final String ADMIN_PASSWORD = "admin4ik_!2";

    @Container
    public static PostgreSQLContainer<?> postgresDB = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test-db")
            .withUsername("test-user")
            .withPassword("test-password");

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresDB::getJdbcUrl);
        registry.add("spring.datasource.username", postgresDB::getUsername);
        registry.add("spring.datasource.password", postgresDB::getPassword);
    }

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        SignupRequest request = SignupRequest.builder()
                .username(MOCK_USERNAME)
                .password(MOCK_PASSWORD)
                .email("user@usermail.ru")
                .build();
        authService.signup(request);

        Optional<Role> adminRole = roleRepository.findRoleByName(RoleEnum.ADMIN);
        User admin = User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .roles(List.of(adminRole.get()))
                .email("admin@admin.ru")
                .build();
        userRepository.save(admin);
    }

    @Test
    void signupTest_validRequestBody_returnsValidResponse() throws Exception {
        // Given
        SignupRequest request = SignupRequest.builder()
                .username("some user")
                .password("some password")
                .email("some@usermail.ru")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/auth/signup")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        SignupResponse response = objectMapper.readValue(jsonResponse, SignupResponse.class);

        // Then
        assertThat(response.getUserId()).isNotNull();
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getRoles()).isNotNull();
        assertThat(response.getRoles()).isNotEmpty();
    }

    @Test
    void loginTest_validUsernameAndPassword_returnsValidResponse() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username(MOCK_USERNAME)
                .password(MOCK_PASSWORD)
                .build();

        var user = userService.loadUserByUsername(MOCK_USERNAME);

        // When
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        LoginResponse response = objectMapper.readValue(jsonResponse, LoginResponse.class);

        // Then
        assertThat(response.getToken()).isNotNull();
        assertThat(jwtService.isTokenValid(response.getToken(), user))
                .isTrue();
    }

    @Test
    void loginTest_existingUserInputsInvalidPassword_returnsHttp400BadRequest() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username(MOCK_USERNAME)
                .password("invalidPassword")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String jsonResponse = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);

        // Then
        assertThat(jsonResponse).contains("Логин и/или пароль были введены неверно!");
    }

    @Test
    void loginTest_notExistingUser_returnsHttp404NotFound() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .username("invalid username")
                .password("invalidPassword")
                .build();

        // When
        // Then
        mockMvc.perform(post("/auth/login")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
    }

    @Test
    void logoutTest_validToken_returnsHttp200OkAndTokenHasNoMorePower() throws Exception {
        // Given
        User user = userRepository.findByUsername(MOCK_USERNAME)
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);

        // When
        // Then
        mockMvc.perform(get("/user/me")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(get("/user/me")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    @Test
    void getAllUsersTest_userHasNoAccessToAllUsersInfo_returnsHttp403() throws Exception {
        // Given
        User user = userRepository.findByUsername(MOCK_USERNAME)
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);

        // When
        // Then
        mockMvc.perform(get("/user")
                        .header(HttpHeaders.AUTHORIZATION, jwtToken))
                .andExpect(status().isForbidden())
                .andReturn();
    }
}
