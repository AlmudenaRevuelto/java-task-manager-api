package taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for the login endpoint.
 *
 * <p>Both fields are required. The password is transmitted in plain text
 * over HTTPS and verified against the BCrypt-encoded value in the database.
 */
@Schema(description = "Credentials used to authenticate a user")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "The user's login name", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "The user's plain-text password", example = "secret123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the password.
     *
     * @return the plain-text password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the plain-text password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}