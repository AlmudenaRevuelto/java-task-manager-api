package taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for the user registration endpoint.
 *
 * <p>The username must be unique. The password will be encoded with BCrypt
 * before being persisted — never stored in plain text.
 */
@Schema(description = "Data required to register a new user")
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Desired login name. Must be unique.", example = "alice", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Plain-text password. Will be BCrypt-encoded before storage.", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * Returns the username.
     *
     * @return the desired username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the desired username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the plain-text password.
     *
     * @return the plain-text password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the plain-text password.
     *
     * @param password the plain-text password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}