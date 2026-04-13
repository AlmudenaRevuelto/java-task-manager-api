package taskmanager.exception;

/**
 * Thrown when the authenticated user attempts to access or modify
 * a resource that belongs to another user.
 */
public class AccessDeniedException extends RuntimeException {

    /**
     * Constructs an {@code AccessDeniedException} with a default message.
     */
    public AccessDeniedException() {
        super("Access denied");
    }
}
