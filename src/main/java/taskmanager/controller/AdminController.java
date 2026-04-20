package taskmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import taskmanager.dto.UpdateRoleRequest;
import taskmanager.dto.UserResponse;
import taskmanager.repository.TaskRepository;
import taskmanager.repository.UserRepository;

import java.util.List;

/**
 * REST controller exposing user-management endpoints accessible only by ADMIN users.
 * All endpoints are served under the {@code /admin} base path.
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "User management operations (ADMIN only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    /**
     * Constructs the controller with its required repositories.
     *
     * @param userRepository repository for user CRUD operations
     * @param taskRepository repository used to cascade-delete a user's tasks before deletion
     */
    public AdminController(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Returns the list of all registered users (id, username, role).
     * Passwords are never included in the response.
     *
     * @return list of {@link UserResponse}
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users", description = "Returns every registered user. Accessible only by ADMINs.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User list returned"),
        @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN")
    })
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getRole()))
                .toList();
    }

    /**
     * Updates the role of an existing user.
     *
     * @param id      the user ID
     * @param request the new role
     * @return the updated {@link UserResponse}
     */
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a user's role", description = "Changes the role (USER / ADMIN) of the specified user. Accessible only by ADMINs.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role value"),
        @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateRoleRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setRole(request.getRole());
        userRepository.save(user);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getUsername(), user.getRole()));
    }

    /**
     * Deletes a user and all their tasks.
     * An admin cannot delete their own account.
     *
     * @param id          the user ID to delete
     * @param currentUser the authenticated admin making the request
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a user", description = "Deletes the user and all their tasks. Admins cannot delete their own account.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Admin tried to delete their own account"),
        @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                            @AuthenticationPrincipal UserDetails currentUser) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (user.getUsername().equals(currentUser.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        taskRepository.deleteByUserId(id);
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}
