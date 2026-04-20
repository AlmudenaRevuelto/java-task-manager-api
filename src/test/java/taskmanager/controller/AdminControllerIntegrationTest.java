package taskmanager.controller;

import tools.jackson.databind.ObjectMapper;
import taskmanager.model.Role;
import taskmanager.model.Task;
import taskmanager.model.User;
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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link AdminController}.
 *
 * <p>Covers all three admin endpoints:
 * <ul>
 *   <li>{@code GET /admin/users} — list all users</li>
 *   <li>{@code PUT /admin/users/{id}/role} — update a user's role</li>
 *   <li>{@code DELETE /admin/users/{id}} — delete a user and their tasks</li>
 * </ul>
 *
 * <p>Method-level security ({@code @PreAuthorize("hasRole('ADMIN')")}) is
 * active in the test profile because {@link taskmanager.config.MethodSecurityConfig}
 * carries no {@code @Profile} restriction.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(taskmanager.config.TestSecurityConfig.class)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    /** The admin user used across tests. */
    private User adminUser;

    /** A regular non-admin user used as a target for admin operations. */
    private User targetUser;

    /**
     * Resets the database before every test: deletes all tasks and users,
     * then seeds an ADMIN ("adminuser") and a regular USER ("targetuser").
     */
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = new User("adminuser", "irrelevant");
        adminUser.setRole(Role.ADMIN);
        adminUser = userRepository.save(adminUser);

        targetUser = new User("targetuser", "irrelevant");
        targetUser.setRole(Role.USER);
        targetUser = userRepository.save(targetUser);
    }

    /** Authenticates the request as the ADMIN user. */
    private static RequestPostProcessor asAdmin() {
        return user("adminuser").roles("ADMIN");
    }

    /** Authenticates the request as a regular USER (should be denied on admin endpoints). */
    private static RequestPostProcessor asUser() {
        return user("targetuser").roles("USER");
    }

    // -----------------------------------------------------------------------
    // GET /admin/users
    // -----------------------------------------------------------------------

    /**
     * Verifies that an ADMIN receives the full user list with id, username, and role.
     */
    @Test
    void shouldListAllUsersAsAdmin() throws Exception {
        mockMvc.perform(get("/admin/users").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].username", containsInAnyOrder("adminuser", "targetuser")))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].role").exists());
    }

    /**
     * Verifies that a regular USER receives 403 when calling GET /admin/users.
     */
    @Test
    void shouldReturn403WhenListingUsersAsUser() throws Exception {
        mockMvc.perform(get("/admin/users").with(asUser()))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that the response never exposes passwords.
     */
    @Test
    void shouldNotExposePasswordsInUserList() throws Exception {
        String body = mockMvc.perform(get("/admin/users").with(asAdmin()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("password"),
                "Response must not contain any 'password' field");
    }

    // -----------------------------------------------------------------------
    // PUT /admin/users/{id}/role
    // -----------------------------------------------------------------------

    /**
     * Verifies that an ADMIN can promote a USER to ADMIN.
     */
    @Test
    void shouldUpdateUserRoleToAdminAsAdmin() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("role", "ADMIN"));

        mockMvc.perform(put("/admin/users/" + targetUser.getId() + "/role")
                .with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUser.getId()))
                .andExpect(jsonPath("$.username").value("targetuser"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // Verify persisted in database
        userRepository.findById(targetUser.getId()).ifPresent(u ->
            org.junit.jupiter.api.Assertions.assertEquals(Role.ADMIN, u.getRole()));
    }

    /**
     * Verifies that an ADMIN can demote an ADMIN to USER.
     */
    @Test
    void shouldUpdateUserRoleToUserAsAdmin() throws Exception {
        // Promote first
        targetUser.setRole(Role.ADMIN);
        userRepository.save(targetUser);

        String body = objectMapper.writeValueAsString(java.util.Map.of("role", "USER"));

        mockMvc.perform(put("/admin/users/" + targetUser.getId() + "/role")
                .with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    /**
     * Verifies that a regular USER receives 403 when trying to change a role.
     */
    @Test
    void shouldReturn403WhenUpdatingRoleAsUser() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("role", "ADMIN"));

        mockMvc.perform(put("/admin/users/" + targetUser.getId() + "/role")
                .with(asUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that updating a non-existent user's role returns 404.
     * The controller throws {@link RuntimeException}, which the
     * {@link taskmanager.exception.GlobalExceptionHandler} maps to 404.
     */
    @Test
    void shouldReturn404WhenUpdatingRoleForNonExistentUser() throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("role", "ADMIN"));

        mockMvc.perform(put("/admin/users/999999/role")
                .with(asAdmin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound());
    }

    // -----------------------------------------------------------------------
    // DELETE /admin/users/{id}
    // -----------------------------------------------------------------------

    /**
     * Verifies that an ADMIN can delete another user. Their tasks are also deleted
     * (cascade via {@code deleteByUserId}).
     */
    @Test
    void shouldDeleteUserAndTheirTasksAsAdmin() throws Exception {
        // Give the target user a task so we also verify cascade delete
        Task task = new Task("Task to be deleted", false);
        task.setUser(targetUser);
        taskRepository.save(task);

        mockMvc.perform(delete("/admin/users/" + targetUser.getId())
                .with(asAdmin()))
                .andExpect(status().isNoContent());

        // User should be gone
        org.junit.jupiter.api.Assertions.assertTrue(
                userRepository.findById(targetUser.getId()).isEmpty(),
                "targetuser should have been deleted");

        // Their tasks should be gone too
        org.junit.jupiter.api.Assertions.assertEquals(0,
                taskRepository.findAll().stream()
                        .filter(t -> t.getUser() != null && t.getUser().getId().equals(targetUser.getId()))
                        .count(),
                "Tasks belonging to targetuser should have been deleted");
    }

    /**
     * Verifies that an ADMIN receives 400 when attempting to delete their own account.
     */
    @Test
    void shouldReturn400WhenAdminDeletesSelf() throws Exception {
        mockMvc.perform(delete("/admin/users/" + adminUser.getId())
                .with(asAdmin()))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that a regular USER receives 403 when trying to delete another user.
     */
    @Test
    void shouldReturn403WhenDeletingUserAsUser() throws Exception {
        mockMvc.perform(delete("/admin/users/" + adminUser.getId())
                .with(asUser()))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that attempting to delete a non-existent user returns 404.
     * The controller throws {@link RuntimeException}, which the
     * {@link taskmanager.exception.GlobalExceptionHandler} maps to 404.
     */
    @Test
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        mockMvc.perform(delete("/admin/users/999999")
                .with(asAdmin()))
                .andExpect(status().isNotFound());
    }
}
