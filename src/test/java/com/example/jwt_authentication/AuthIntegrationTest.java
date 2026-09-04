package com.example.jwt_authentication;

import com.example.jwt_authentication.entity.Role;
import com.example.jwt_authentication.entity.User;
import com.example.jwt_authentication.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        userRepository.deleteAll();

        User user = new User(
                "testuser",
                "testuser@example.com",
                passwordEncoder.encode("Test@1234"),
                Role.USER
        );

        userRepository.save(user);
    }

    @Test
    void loginSuccess() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "username": "testuser",
                                    "password": "Test@1234"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void wrongPasswordReturnsUnauthorized() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "username": "testuser",
                                    "password": "WrongPassword"
                                }
                                """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid username or password"))
                .andExpect(jsonPath("$.status")
                        .value(401));
    }

    @Test
    void unknownUsernameReturnsUnauthorized() throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "username": "doesnotexist",
                                    "password": "Test@1234"
                                }
                                """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid username or password"))
                .andExpect(jsonPath("$.status")
                        .value(401));
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/api/test")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401));
    }

    @Test
    void userCanAccessProtectedEndpoint()
            throws Exception {

        String token = loginAndGetToken();

        mockMvc.perform(
                        get("/api/test")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "You are authenticated!"
                        )
                );
    }

    @Test
    void userCanAccessOwnProfile()
            throws Exception {

        String token = loginAndGetToken();

        mockMvc.perform(
                        get("/api/users/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username")
                        .value("testuser"))
                .andExpect(jsonPath("$.email")
                        .value("testuser@example.com"))
                .andExpect(jsonPath("$.role")
                        .value("USER"));
    }

    @Test
    void invalidJwtReturnsUnauthorized()
            throws Exception {

        mockMvc.perform(
                        get("/api/test")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Invalid or expired token"))
                .andExpect(jsonPath("$.status")
                        .value(401));
    }

    @Test
    void userCannotAccessAdminEndpoint()
            throws Exception {

        String token = loginAndGetToken();

        mockMvc.perform(
                        get("/api/admin")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAccessAdminEndpoint()
            throws Exception {

        User admin = new User(
                "admin",
                "admin@example.com",
                passwordEncoder.encode("Admin@1234"),
                Role.ADMIN
        );

        userRepository.save(admin);

        String response =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content("""
                                        {
                                            "username": "admin",
                                            "password": "Admin@1234"
                                        }
                                        """)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        String token =
                objectMapper
                        .readTree(response)
                        .get("token")
                        .asText();

        mockMvc.perform(
                        get("/api/admin")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().string(
                                "You are an admin!"
                        )
                );
    }

    @Test
    void duplicateUsernameReturnsConflict()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "username": "testuser",
                                    "email": "different@example.com",
                                    "password": "Password@123"
                                }
                                """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Username already exists"))
                .andExpect(jsonPath("$.status")
                        .value(409));
    }

    @Test
    void duplicateEmailReturnsConflict()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "username": "differentuser",
                                    "email": "testuser@example.com",
                                    "password": "Password@123"
                                }
                                """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Email already exists"))
                .andExpect(jsonPath("$.status")
                        .value(409));
    }

    @Test
    void invalidRegistrationReturnsBadRequest()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                {
                                    "username": "ab",
                                    "email": "invalid-email",
                                    "password": "123"
                                }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.errors.username")
                        .exists())
                .andExpect(jsonPath("$.errors.email")
                        .exists())
                .andExpect(jsonPath("$.errors.password")
                        .exists());
    }

    private String loginAndGetToken()
            throws Exception {

        String response =
                mockMvc.perform(
                                post("/api/auth/login")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content("""
                                        {
                                            "username": "testuser",
                                            "password": "Test@1234"
                                        }
                                        """)
                        )
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString();

        return objectMapper
                .readTree(response)
                .get("token")
                .asText();
    }
}