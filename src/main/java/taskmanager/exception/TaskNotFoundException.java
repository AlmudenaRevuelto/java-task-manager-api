package taskmanager.exception;

/**
 * Exception thrown when a task with the requested ID does not exist.
 *
 * <p>Handled by {@link GlobalExceptionHandler}, which maps it to a
 * {@code 404 Not Found} response.
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * Constructs a new exception for the given task ID.
     *
     * @param id the ID of the task that was not found
     */
    public TaskNotFoundException(Long id) {
        super("Task not found: " + id);
    }
}
