package taskmanager.controller;

import tools.jackson.databind.ObjectMapper;
import taskmanager.dto.auth.RegisterRequest;
import taskmanager.model.Role;
import taskmanager.repository.TaskRepository;
import taskmanager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AuthController}.
 *
 * <p>Verifies the registration endpoint ({@code POST /auth/register}):
 * default-role assignment, explicit-role assignment, and duplicate-username
 * rejection. The login endpoint is not tested here because its behaviour
 * (credential validation and JWT issuance) is already covered by the Spring
 * Security authentication stack.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(taskmanager.config.TestSecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /** Clears tasks and users before each test to ensure a clean state. */
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Verifies that a user registered without a {@code role} field is assigned
     * {@link Role#USER} by default, and that the response contains a JWT token
     * and the username.
     */
    @Test
    void shouldRegisterUserWithDefaultRoleUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alice");
        request.setPassword("password123");
        // role not set → should default to USER

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.role").value("USER"));

        // Verify the role was persisted as USER
        userRepository.findByUsername("alice").ifPresentOrElse(
            user -> org.junit.jupiter.api.Assertions.assertEquals(Role.USER, user.getRole(),
                "Expected role USER but was " + user.getRole()),
            () -> org.junit.jupiter.api.Assertions.fail("User 'alice' was not found in the database")
        );
    }

    /**
     * Verifies that a user registered with an explicit {@link Role#ADMIN} is
     * persisted with that role.
     */
    @Test
    void shouldRegisterUserWithExplicitRoleAdmin() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("superadmin");
        request.setPassword("adminpass");
        request.setRole(Role.ADMIN);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("superadmin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Verify the role was persisted as ADMIN
        userRepository.findByUsername("superadmin").ifPresentOrElse(
            user -> org.junit.jupiter.api.Assertions.assertEquals(Role.ADMIN, user.getRole(),
                "Expected role ADMIN but was " + user.getRole()),
            () -> org.junit.jupiter.api.Assertions.fail("User 'superadmin' was not found in the database")
        );
    }

    /**
     * Verifies that registering with an explicit {@link Role#USER} also works
     * (explicit value matching the default should still be accepted).
     */
    @Test
    void shouldRegisterUserWithExplicitRoleUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("bob");
        request.setPassword("bobpass");
        request.setRole(Role.USER);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.role").value("USER"));

        userRepository.findByUsername("bob").ifPresentOrElse(
            user -> org.junit.jupiter.api.Assertions.assertEquals(Role.USER, user.getRole(),
                "Expected role USER but was " + user.getRole()),
            () -> org.junit.jupiter.api.Assertions.fail("User 'bob' was not found in the database")
        );
    }

    /**
     * Verifies that attempting to register with a username that is already taken
     * returns {@code 400 Bad Request}.
     */
    @Test
    void shouldReturn400WhenUsernameAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicate");
        request.setPassword("pass1");

        // First registration — should succeed
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with the same username — should fail
        request.setPassword("pass2");
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that registering without a username returns {@code 400 Bad Request}
     * with a validation error message.
     */
    @Test
    void shouldReturn400WhenUsernameIsBlank() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setPassword("somepass");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    /**
     * Verifies that a successful login returns the JWT token, the username,
     * and the correct role in the response body.
     */
    @Test
    void shouldLoginAndReturnRoleInResponse() throws Exception {
        // Register first so the user exists in H2
        RegisterRequest register = new RegisterRequest();
        register.setUsername("carol");
        register.setPassword("carolpass");
        register.setRole(Role.ADMIN);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Login and verify role is returned
        taskmanager.dto.auth.LoginRequest login = new taskmanager.dto.auth.LoginRequest();
        login.setUsername("carol");
        login.setPassword("carolpass");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("carol"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
