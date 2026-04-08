package taskmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import taskmanager.model.Priority;

@Schema(description = "Task data returned by the API")
public class TaskResponse {
    @Schema(description = "Unique task identifier", example = "1")
    private Long id;

    @Schema(description = "Task title", example = "Buy groceries")
    private String title;

    @Schema(description = "Optional task description", example = "Milk, eggs, bread")
    private String description;

    @Schema(description = "Whether the task is completed", example = "false")
    private boolean completed;

    @Schema(description = "Task priority", example = "HIGH")
    private Priority priority;

    @Schema(description = "Optional due date", example = "2026-12-31")
    private LocalDate dueDate;

    @Schema(description = "Timestamp when the task was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update")
    private LocalDateTime updatedAt;

    public TaskResponse(
            Long id,
            String title,
            String description,
            boolean completed,
            Priority priority,
            LocalDate dueDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
        this.priority = priority;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public Priority getPriority() { return priority; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}