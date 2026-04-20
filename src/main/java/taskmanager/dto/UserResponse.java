package taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import taskmanager.model.Role;
import taskmanager.model.User;

/**
 * Read-only projection of a {@link User} returned by the admin endpoints.
 * Never exposes the encoded password.
 */
@Schema(description = "User data exposed to administrators")
public class UserResponse {

    @Schema(description = "Internal user ID", example = "1")
    private Long id;

    @Schema(description = "Unique login name", example = "alice")
    private String username;

    @Schema(description = "Role assigned to the user", example = "USER", allowableValues = {"USER", "ADMIN"})
    private Role role;

    public UserResponse(Long id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Role getRole() { return role; }
}
