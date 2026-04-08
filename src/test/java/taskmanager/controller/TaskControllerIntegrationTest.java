package taskmanager.controller;


import tools.jackson.databind.ObjectMapper;
import taskmanager.dto.TaskRequest;
import taskmanager.model.Priority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
}
