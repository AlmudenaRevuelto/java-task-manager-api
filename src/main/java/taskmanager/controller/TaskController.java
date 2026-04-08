package taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import taskmanager.dto.TaskRequest;
import taskmanager.dto.TaskResponse;
import taskmanager.service.TaskService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller that exposes CRUD endpoints for managing tasks.
 * All endpoints are served under the /tasks base path.
 */
@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "CRUD operations for task management")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    /**
     * Returns a list of all tasks.
     *
     * @return list of {@link TaskResponse}
     */
    @GetMapping
    @Operation(summary = "List all tasks", description = "Returns all tasks sorted by creation order.")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    public List<TaskResponse> getAll() {
        return service.getAll();
    }

    /**
     * Returns a single task by its ID.
     *
     * @param id the task ID
     * @return the matching {@link TaskResponse}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID", description = "Returns a single task matching the provided ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public TaskResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    /**
     * Creates a new task.
     *
     * @param request the task data
     * @return the created {@link TaskResponse} with HTTP 201
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task. Title is required. Priority defaults to MEDIUM if not provided.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Task created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error — title is blank or request is malformed")
    })
    public TaskResponse create(@Valid @RequestBody TaskRequest request) {
        return service.create(request);
    }

    /**
     * Updates an existing task by its ID.
     *
     * @param id      the task ID
     * @param request the updated task data
     * @return the updated {@link TaskResponse}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing task by ID", description = "Replaces all fields of an existing task. Title is required.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the task ID
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task by ID", description = "Permanently removes the task with the given ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}