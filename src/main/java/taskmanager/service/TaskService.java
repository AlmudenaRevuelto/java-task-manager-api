package taskmanager.service;

import org.springframework.stereotype.Service;
import java.util.List;

import taskmanager.model.Task;
import taskmanager.repository.TaskRepository;
import taskmanager.dto.TaskRequest;
import taskmanager.dto.TaskResponse;

/**
 * Service layer containing the business logic for task management.
 */
@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    /**
     * Maps a {@link Task} entity to a {@link TaskResponse} DTO.
     *
     * @param task the entity to map
     * @return the corresponding DTO
     */
    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.isCompleted(),
            task.getPriority(),
            task.getDueDate(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }

    /**
     * Returns all tasks.
     *
     * @return list of all tasks as {@link TaskResponse}
     */
    public List<TaskResponse> getAll() {
        return repository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Creates and persists a new task.
     *
     * @param request the task data
     * @return the created task as {@link TaskResponse}
     */
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(request.getTitle(), request.isCompleted());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        Task saved = repository.save(task);
        return toResponse(saved);
    }

    /**
     * Updates an existing task.
     *
     * @param id      the ID of the task to update
     * @param request the new task data
     * @return the updated task as {@link TaskResponse}
     * @throws RuntimeException if no task is found with the given ID
     */
    public TaskResponse update(Long id, TaskRequest request) {
        Task existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setCompleted(request.isCompleted());
        existing.setPriority(request.getPriority());
        existing.setDueDate(request.getDueDate());

        Task updated = repository.save(existing);
        return toResponse(updated);
    }

    /**
     * Returns a single task by its ID.
     *
     * @param id the task ID
     * @return the task as {@link TaskResponse}
     * @throws RuntimeException if no task is found with the given ID
     */
    public TaskResponse getById(Long id) {
        Task task = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Task not found: " + id));
        return toResponse(task);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the task ID
     */
    public void delete(Long id) {
        repository.deleteById(id);
    }
}