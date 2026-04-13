package taskmanager.controller;


import tools.jackson.databind.ObjectMapper;
import taskmanager.dto.TaskRequest;
import taskmanager.model.Priority;
import taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link TaskController}.
 * Uses MockMvc to simulate HTTP requests against a full Spring context
 * backed by an in-memory H2 database ("test" profile).
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

    @BeforeEach
    void cleanUp() {
        taskRepository.deleteAll();
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("Title is required"));
    }

    /** Verifies that GET /tasks/{id} returns 404 for a non-existent task. */
    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockMvc.perform(get("/tasks/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found: 999999"));
    }

    /** Verifies that PUT /tasks/{id} updates an existing task and returns the updated data. */
    @Test
    void shouldUpdateTask() throws Exception {
        // Create a task first
        TaskRequest createRequest = new TaskRequest();
        createRequest.setTitle("Old Title");
        createRequest.setCompleted(false);

        String response = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Actualizarla
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Descripción actualizada");
        updateRequest.setCompleted(true);
        updateRequest.setPriority(Priority.LOW);
        updateRequest.setDueDate(LocalDate.of(2026, 6, 1));

        mockMvc.perform(put("/tasks/" + id)
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
        // Create a task to delete
        TaskRequest request = new TaskRequest();
        request.setTitle("Task To Delete");
        request.setCompleted(false);

        String response = mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        // Delete the task
        mockMvc.perform(delete("/tasks/" + id))
                .andExpect(status().isNoContent());

        // Verificar que ya no existe
        mockMvc.perform(get("/tasks/" + id))
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

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(done)));
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pending)));

        mockMvc.perform(get("/tasks").param("completed", "true"))
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

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(high)));
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(low)));

        mockMvc.perform(get("/tasks").param("priority", "HIGH"))
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
            mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)));
        }

        mockMvc.perform(get("/tasks").param("size", "2").param("page", "0"))
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

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)));
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)));

        String body = mockMvc.perform(get("/tasks").param("sort", "dueDate,asc"))
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

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matching)));
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notMatching)));

        mockMvc.perform(get("/tasks").param("search", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring Boot Guide"));

        mockMvc.perform(get("/tasks").param("search", "comprehensive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].description").value("A comprehensive guide to Spring Boot"));

        mockMvc.perform(get("/tasks").param("search", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }
}
