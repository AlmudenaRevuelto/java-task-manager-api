package taskmanager.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned by the login and register endpoints.
 *
 * <p>Contains the signed JWT token and the authenticated username.
 * The token must be included in subsequent requests as a Bearer token:
 * {@code Authorization: Bearer <token>}
 */
@Schema(description = "JWT authentication response")
public class JwtAuthResponse {

    @Schema(description = "Signed JWT token to use in the Authorization header", example = "eyJhbGci...")
    private final String token;

    @Schema(description = "Username of the authenticated user", example = "admin")
    private final String username;

    public JwtAuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
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
}
