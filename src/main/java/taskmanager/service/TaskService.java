package taskmanager.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import taskmanager.model.Task;
import taskmanager.repository.TaskRepository;
import taskmanager.repository.UserRepository;
import taskmanager.specification.TaskSpecification;
import taskmanager.dto.TaskRequest;
import taskmanager.dto.TaskResponse;

import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Priority;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Service layer containing the business logic for task management.
 */
@Service
public class TaskService {

    private final TaskRepository repository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
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
     * Returns paginated and filtered tasks.
     *
     * @param completed optional completed filter
     * @param priority optional priority filter
     * @param dueBefore optional due date filter
     * @param search optional free-text search over title and description
     * @param pageable pagination/sorting info
     * @return paginated task responses
     */
    public Page<TaskResponse> getAll(
            Boolean completed,
            Priority priority,
            LocalDate dueBefore,
            String search,
            Pageable pageable) {

        String username = getCurrentUsername();

        Specification<Task> spec = Specification
                .where(TaskSpecification.belongsToUser(username))
                .and(TaskSpecification.hasCompleted(completed))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.dueBefore(dueBefore))
                .and(TaskSpecification.containsText(search));

        return repository.findAll(spec, pageable)
                .map(this::toResponse);
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
        String username = getCurrentUsername();
        task.setUser(userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username)));
        Task saved = repository.save(task);
        return toResponse(saved);
    }

    /**
     * Updates an existing task.
     *
     * @param id      the ID of the task to update
     * @param request the new task data
     * @return the updated task as {@link TaskResponse}
     * @throws RuntimeException if no task is found with the given ID or access is denied
     */
    public TaskResponse update(Long id, TaskRequest request) {
        String username = getCurrentUsername();

        Task existing = repository.findByIdAndUserUsername(id, username)
            .orElseThrow(() -> new TaskNotFoundException(id));

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
        String username = getCurrentUsername();

        Task task = repository.findByIdAndUserUsername(id, username)
            .orElseThrow(() -> new TaskNotFoundException(id));

        return toResponse(task);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the task ID
     */
    public void delete(Long id) {
        String username = getCurrentUsername();

        Task task = repository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new TaskNotFoundException(id));

        repository.delete(task);
    }

    /**
     * Resolves the username of the currently authenticated user from the {@link SecurityContextHolder}.
     *
     * @return the username of the authenticated principal
     */
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return principal.toString();
    }
}