package taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import taskmanager.model.Role;

/**
 * Response payload returned by the login and register endpoints.
 *
 * <p>Contains the signed JWT token, the authenticated username, and the user's role.
 * The token must be included in subsequent requests as a Bearer token:
 * {@code Authorization: Bearer <token>}
 */
@Schema(description = "JWT authentication response")
public class JwtAuthResponse {

    @Schema(description = "Signed JWT token to use in the Authorization header", example = "eyJhbGci...")
    private final String token;

    @Schema(description = "Username of the authenticated user", example = "admin")
    private final String username;

    /** Role assigned to the authenticated user. */
    @Schema(description = "Role of the authenticated user", example = "USER", allowableValues = {"USER", "ADMIN"})
    private final Role role;

    /**
     * Constructs a JWT auth response.
     *
     * @param token    the signed JWT token
     * @param username the authenticated username
     * @param role     the role assigned to the user
     */
    public JwtAuthResponse(String token, String username, Role role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }

    /**
     * Returns the JWT token.
     *
     * @return the signed token string
     */
    public String getToken() {
        return token;
    }

    /**
     * Returns the authenticated username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the role of the authenticated user.
     *
     * @return the {@link Role}
     */
    public Role getRole() {
        return role;
    }
}
