package taskmanager.exception;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Standardised error body returned by {@link GlobalExceptionHandler} on all
 * error responses. Serialised as JSON.
 */
@Schema(description = "Error response body returned when a request fails")
public class ErrorResponse {

    @Schema(description = "Short human-readable error summary", example = "Task not found: 42")
    private String message;

    @Schema(description = "Optional list of detailed validation error messages", example = "[\"Title is required\"]")
    private List<String> details;

    /**
     * Constructs an error response.
     *
     * @param message a short summary of the error
     * @param details optional list of per-field validation messages; may be {@code null}
     */
    public ErrorResponse(String message, List<String> details) {
        this.message = message;
        this.details = details;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getMessage() { return message; }

    /**
     * Returns the list of detailed error messages.
     *
     * @return the details, or {@code null} if not applicable
     */
    public List<String> getDetails() { return details; }
}