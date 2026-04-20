package taskmanager.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import taskmanager.model.Priority;

@Schema(description = "Payload for creating or updating a task")
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Schema(description = "Task title", example = "Buy groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Optional task description", example = "Milk, eggs, bread")
    private String description;

    @Schema(description = "Whether the task is completed", example = "false")
    private boolean completed;

    @Schema(description = "Task priority. Defaults to MEDIUM if not provided", example = "HIGH", allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private Priority priority;

    @Schema(description = "Optional due date in ISO format", example = "2026-12-31")
    private LocalDate dueDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

}