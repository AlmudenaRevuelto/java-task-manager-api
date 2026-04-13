package taskmanager.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import taskmanager.model.Task;
import taskmanager.repository.TaskRepository;
import taskmanager.repository.UserRepository;
import taskmanager.security.SecurityHelper;
import taskmanager.specification.TaskSpecification;
import taskmanager.dto.TaskRequest;
import taskmanager.dto.TaskResponse;

import taskmanager.exception.TaskNotFoundException;
import taskmanager.model.Priority;

/**
 * Service layer containing the business logic for task management.
 *
 * <p>Security concerns (resolving the current user, admin checks, ownership
 * enforcement) are delegated to {@link SecurityHelper}, keeping this class
 * focused on domain logic only.
 */
@Service
public class TaskService {

    private final TaskRepository repository;
    private final UserRepository userRepository;
    private final SecurityHelper securityHelper;

    /**
     * Constructs the service with its required repositories and security helper.
     *
     * @param repository     the task repository
     * @param userRepository the user repository
     * @param securityHelper helper for resolving the authenticated user and enforcing ownership
     */
    public TaskService(TaskRepository repository, UserRepository userRepository,
                       SecurityHelper securityHelper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.securityHelper = securityHelper;
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
     * <p>Admins see all tasks; regular users see only their own.
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

        String username = securityHelper.getCurrentUsername();

        Specification<Task> ownerFilter = securityHelper.isAdmin()
            ? (root, query, cb) -> cb.conjunction()
            : TaskSpecification.belongsToUser(username);

        Specification<Task> spec = Specification
            .where(ownerFilter)
            .and(TaskSpecification.hasCompleted(completed))
            .and(TaskSpecification.hasPriority(priority))
            .and(TaskSpecification.dueBefore(dueBefore))
            .and(TaskSpecification.containsText(search));

        return repository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    /**
     * Creates and persists a new task assigned to the current user.
     *
     * @param request the task data
     * @return the created task as {@link TaskResponse}
     */
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(request.getTitle(), request.isCompleted());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        String username = securityHelper.getCurrentUsername();
        task.setUser(userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username)));
        return toResponse(repository.save(task));
    }

    /**
     * Updates an existing task.
     *
     * @param id      the ID of the task to update
     * @param request the new task data
     * @return the updated task as {@link TaskResponse}
     * @throws TaskNotFoundException  if no task is found with the given ID
     * @throws taskmanager.exception.AccessDeniedException if the current user is not the owner and not an admin
     */
    public TaskResponse update(Long id, TaskRequest request) {
        Task existing = repository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        securityHelper.checkOwnership(existing);

        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setCompleted(request.isCompleted());
        existing.setPriority(request.getPriority());
        existing.setDueDate(request.getDueDate());

        return toResponse(repository.save(existing));
    }

    /**
     * Returns a single task by its ID.
     *
     * @param id the task ID
     * @return the task as {@link TaskResponse}
     * @throws TaskNotFoundException if no task is found with the given ID
     * @throws taskmanager.exception.AccessDeniedException if the current user is not the owner and not an admin
     */
    public TaskResponse getById(Long id) {
        Task task = repository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));

        securityHelper.checkOwnership(task);

        return toResponse(task);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the task ID
     * @throws TaskNotFoundException if no task is found with the given ID
     * @throws taskmanager.exception.AccessDeniedException if the current user is not the owner and not an admin
     */
    public void delete(Long id) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        securityHelper.checkOwnership(task);

        repository.delete(task);
    }
}
