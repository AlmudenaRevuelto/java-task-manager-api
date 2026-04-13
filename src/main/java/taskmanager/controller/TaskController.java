package taskmanager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import taskmanager.dto.TaskRequest;
import taskmanager.dto.TaskResponse;
import taskmanager.model.Priority;
import taskmanager.service.TaskService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * REST controller that exposes CRUD endpoints for managing tasks.
 * All endpoints are served under the /tasks base path.
 */
@RestController
@RequestMapping("/tasks")
@Tag(name = "Tasks", description = "CRUD operations for task management")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "basicAuth")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    /**
     * Returns a paginated, filtered and optionally sorted list of tasks.
     *
     * @param completed optional filter by completion status
     * @param priority  optional filter by priority level
     * @param dueBefore optional filter for tasks due on or before this date
     * @param search    optional free-text search over title and description
     * @param pageable  pagination and sorting parameters
     * @return paginated list of {@link TaskResponse}
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(
        summary = "List tasks",
        description = "Returns a paginated list of tasks. Supports filtering by completion status, priority, due date, and free-text search over title and description."
    )
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    public Page<TaskResponse> getAll(
            @Parameter(description = "Filter by completion status") @RequestParam(required = false) Boolean completed,
            @Parameter(description = "Filter by priority level (LOW, MEDIUM, HIGH)") @RequestParam(required = false) Priority priority,
            @Parameter(description = "Return tasks due on or before this date (ISO format: yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
            @Parameter(description = "Free-text search over title and description (case-insensitive)") @RequestParam(required = false) String search,
            @ParameterObject Pageable pageable) {
        return service.getAll(completed, priority, dueBefore, search, pageable);
    }

    /**
     * Returns a single task by its ID.
     *
     * @param id the task ID
     * @return the matching {@link TaskResponse}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Delete a task by ID", description = "Permanently removes the task with the given ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}