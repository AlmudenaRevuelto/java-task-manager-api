package taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import taskmanager.model.Role;

/**
 * Request payload for the PATCH/PUT role-update endpoint.
 */
@Schema(description = "Role to assign to a user")
public class UpdateRoleRequest {

    @NotNull(message = "Role is required")
    @Schema(description = "New role for the user", example = "ADMIN", allowableValues = {"USER", "ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private Role role;

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
