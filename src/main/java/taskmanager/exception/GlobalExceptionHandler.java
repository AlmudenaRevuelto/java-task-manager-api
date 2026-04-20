package taskmanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Centralised exception handler for the Task Manager API.
 *
 * <p>Intercepts exceptions thrown by controllers and maps them to a consistent
 * {@link ErrorResponse} JSON body. Registered globally via {@link RestControllerAdvice}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles bean validation errors (e.g. {@code @NotBlank}, {@code @NotNull}).
     * Returns HTTP 400 with a list of field error messages.
     *
     * @param ex the validation exception thrown by Spring MVC
     * @return a {@code 400 Bad Request} response containing all validation messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .toList();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation failed", errors));
    }

    /**
     * Handles {@link RuntimeException} thrown when a resource is not found.
     * Returns HTTP 404 with the exception message.
     *
     * @param ex the runtime exception, typically a {@link TaskNotFoundException}
     * @return a {@code 404 Not Found} response with the error message
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleNotFound(RuntimeException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Handles the custom {@link taskmanager.exception.AccessDeniedException} thrown by
     * {@link taskmanager.security.SecurityHelper#checkOwnership} when a user tries to
     * access a resource that belongs to another user.
     * Returns HTTP 403 with the exception message.
     *
     * @param ex the access denied exception
     * @return a {@code 403 Forbidden} response with the error message
     */
    @ExceptionHandler(taskmanager.exception.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(taskmanager.exception.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Handles Spring Security's {@link AccessDeniedException} thrown by
     * {@code @PreAuthorize} checks when a user lacks the required role.
     * This ensures that role-based denials return {@code 403 Forbidden} rather
     * than leaking as a generic {@code 404} via the {@link RuntimeException} handler.
     *
     * @param ex the Spring Security access denied exception
     * @return a {@code 403 Forbidden} response
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                   .body(new ErrorResponse("Access Denied", null));
    }
}