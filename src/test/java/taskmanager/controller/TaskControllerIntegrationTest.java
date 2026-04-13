package taskmanager.controller;


import tools.jackson.databind.ObjectMapper;
import taskmanager.dto.TaskRequest;
import taskmanager.model.Priority;
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

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link TaskController}.
 * Uses MockMvc to simulate HTTP requests against a full Spring context
 * backed by an in-memory H2 database ("test" profile).
 *
 * <p>Each helper method ({@link #asTestUser()}, {@link #asAdminUser()},
 * {@link #asOtherUser()}) injects the desired principal directly into the
 * MockMvc request via {@code SecurityMockMvcRequestPostProcessors.user()},
 * which is reliable across Spring Security 6.x where
 * {@code SecurityContextHolderFilter} would otherwise overwrite a
 * {@code @WithMockUser}-supplied context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(taskmanager.config.TestSecurityConfig.class)
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /** The regular test user, saved in H2 before each test. */
    private User testUser;

    /**
     * Resets the database before every test: deletes all tasks and users,
     * then creates a USER ("testuser") and an ADMIN ("adminuser") in H2 so
     * that service-level user lookups succeed.
     */
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("testuser", "irrelevant");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        User adminUser = new User("adminuser", "irrelevant");
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);
    }

    /** Post-processor that authenticates requests as the regular test user (role USER). */
    private static RequestPostProcessor asTestUser() {
        return user("testuser").roles("USER");
    }

    /** Post-processor that authenticates requests as the admin user (role ADMIN). */
    private static RequestPostProcessor asAdminUser() {
        return user("adminuser").roles("ADMIN");
    }

    /** Post-processor that authenticates requests as an unrelated user (role USER). */
    private static RequestPostProcessor asOtherUser() {
        return user("otheruser").roles("USER");
    }

    /** Verifies that POST /tasks creates a task and returns 201 with all fields. */
    @Test
    void shouldCreateTask() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Learn Testing");
        request.setDescription("Una descripción de prueba");
        request.setCompleted(false);
        request.setPriority(Priority.HIGH);
        request.setDueDate(LocalDate.of(2026, 12, 31));

        mockMvc.perform(post("/tasks")
                .with(asTestUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Learn Testing"))
                .andExpect(jsonPath("$.description").value("Una descripción de prueba"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-31"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    /** Verifies that POST /tasks returns 400 when the title is blank. */
    @Test
    void shouldReturn400WhenTitleIsBlank() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("");
        request.setCompleted(false);

        mockMvc.perform(post("/tasks")
                .with(asTestUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("Title is required"));
    }

    /** Verifies that GET /tasks/{id} returns 404 for a non-existent task. */
    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/tasks/999999")
                .with(asTestUser()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: 999999"));
    }

    /** Verifies that PUT /tasks/{id} updates an existing task and returns the updated data. */
    @Test
    void shouldUpdateTask() throws Exception {
        TaskRequest createRequest = new TaskRequest();
        createRequest.setTitle("Old Title");
        createRequest.setCompleted(false);

        String response = mockMvc.perform(post("/tasks")
                .with(asTestUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Descripción actualizada");
        updateRequest.setCompleted(true);
        updateRequest.setPriority(Priority.LOW);
        updateRequest.setDueDate(LocalDate.of(2026, 6, 1));

        mockMvc.perform(put("/tasks/" + id)
                .with(asTestUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Descripción actualizada"))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.dueDate").value("2026-06-01"));
    }

    /** Verifies that DELETE /tasks/{id} removes the task and subsequent GET returns 404. */
    @Test
    void shouldDeleteTask() throws Exception {
        TaskRequest request = new TaskRequest();
        request.setTitle("Task To Delete");
        request.setCompleted(false);

        String response = mockMvc.perform(post("/tasks")
                .with(asTestUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/tasks/" + id)
                .with(asTestUser()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/tasks/" + id)
                .with(asTestUser()))
                .andExpect(status().isNotFound());
    }

    /** Verifies that GET /tasks?completed=true returns only completed tasks. */
    @Test
    void shouldFilterTasksByCompleted() throws Exception {
        TaskRequest done = new TaskRequest();
        done.setTitle("Completed Task");
        done.setCompleted(true);

        TaskRequest pending = new TaskRequest();
        pending.setTitle("Pending Task");
        pending.setCompleted(false);

        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(done)));
        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pending)));

        mockMvc.perform(get("/tasks").with(asTestUser()).param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].completed").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is(true))));
    }

    /** Verifies that GET /tasks?priority=HIGH returns only HIGH priority tasks. */
    @Test
    void shouldFilterTasksByPriority() throws Exception {
        TaskRequest high = new TaskRequest();
        high.setTitle("High Priority Task");
        high.setCompleted(false);
        high.setPriority(Priority.HIGH);

        TaskRequest low = new TaskRequest();
        low.setTitle("Low Priority Task");
        low.setCompleted(false);
        low.setPriority(Priority.LOW);

        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(high)));
        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(low)));

        mockMvc.perform(get("/tasks").with(asTestUser()).param("priority", "HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].priority").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("HIGH"))));
    }

    /** Verifies that GET /tasks?size=2 returns at most 2 tasks per page. */
    @Test
    void shouldPaginateTasks() throws Exception {
        for (int i = 1; i <= 3; i++) {
            TaskRequest req = new TaskRequest();
            req.setTitle("Paginated Task " + i);
            req.setCompleted(false);
            mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)));
        }

        mockMvc.perform(get("/tasks").with(asTestUser()).param("size", "2").param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(2)))
                .andExpect(jsonPath("$.pageable.pageSize").value(2));
    }

    /** Verifies that GET /tasks?sort=dueDate,asc returns tasks ordered by dueDate ascending. */
    @Test
    void shouldSortTasksByDueDate() throws Exception {
        TaskRequest first = new TaskRequest();
        first.setTitle("Earlier Task");
        first.setCompleted(false);
        first.setDueDate(LocalDate.of(2026, 1, 1));

        TaskRequest second = new TaskRequest();
        second.setTitle("Later Task");
        second.setCompleted(false);
        second.setDueDate(LocalDate.of(2026, 12, 31));

        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)));
        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)));

        String body = mockMvc.perform(get("/tasks").with(asTestUser()).param("sort", "dueDate,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sort.sorted").value(true))
                .andReturn().getResponse().getContentAsString();

        tools.jackson.databind.JsonNode content = objectMapper.readTree(body).get("content");
        for (int i = 0; i < content.size() - 1; i++) {
            String current = content.get(i).get("dueDate").asText();
            String next = content.get(i + 1).get("dueDate").asText();
            if (!current.equals("null") && !next.equals("null")) {
                org.junit.jupiter.api.Assertions.assertTrue(current.compareTo(next) <= 0,
                        "Expected dueDate[" + i + "] <= dueDate[" + (i + 1) + "] but got " + current + " > " + next);
            }
        }
    }

    /** Verifies that GET /tasks?search=... returns only tasks matching title or description. */
    @Test
    void shouldSearchTasksByTitleOrDescription() throws Exception {
        TaskRequest matching = new TaskRequest();
        matching.setTitle("Spring Boot Guide");
        matching.setDescription("A comprehensive guide to Spring Boot");
        matching.setCompleted(false);

        TaskRequest notMatching = new TaskRequest();
        notMatching.setTitle("Grocery list");
        notMatching.setDescription("Buy milk and eggs");
        notMatching.setCompleted(false);

        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matching)));
        mockMvc.perform(post("/tasks").with(asTestUser()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notMatching)));

        mockMvc.perform(get("/tasks").with(asTestUser()).param("search", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Guide"));

        mockMvc.perform(get("/tasks").with(asTestUser()).param("search", "comprehensive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].description").value("A comprehensive guide to Spring Boot"));

        mockMvc.perform(get("/tasks").with(asTestUser()).param("search", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    /**
     * Verifies that an ADMIN can read a task that belongs to a different user.
     * The task is inserted directly in the repository (assigned to "testuser")
     * and fetched as "adminuser".
     */
    @Test
    void shouldAdminAccessAnotherUsersTask() throws Exception {
        Task task = new Task("Admin Visible Task", false);
        task.setUser(testUser);
        task = taskRepository.save(task);

        mockMvc.perform(get("/tasks/" + task.getId())
                .with(asAdminUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Admin Visible Task"));
    }

    /**
     * Verifies that a regular USER receives 403 when trying to access a task
     * that belongs to a different user.
     */
    @Test
    void shouldReturn403WhenAccessingOtherUsersTask() throws Exception {
        Task task = new Task("Private Task", false);
        task.setUser(testUser);
        task = taskRepository.save(task);

        mockMvc.perform(get("/tasks/" + task.getId())
                .with(asOtherUser()))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that a regular USER receives 403 when trying to update a task
     * that belongs to a different user.
     */
    @Test
    void shouldReturn403WhenUpdatingOtherUsersTask() throws Exception {
        Task task = new Task("Private Task", false);
        task.setUser(testUser);
        task = taskRepository.save(task);

        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Hacked Title");
        updateRequest.setCompleted(false);

        mockMvc.perform(put("/tasks/" + task.getId())
                .with(asOtherUser())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    /**
     * Verifies that a regular USER receives 403 when trying to delete a task
     * that belongs to a different user.
     */
    @Test
    void shouldReturn403WhenDeletingOtherUsersTask() throws Exception {
        Task task = new Task("Private Task", false);
        task.setUser(testUser);
        task = taskRepository.save(task);

        mockMvc.perform(delete("/tasks/" + task.getId())
                .with(asOtherUser()))
                .andExpect(status().isForbidden());
    }
}
