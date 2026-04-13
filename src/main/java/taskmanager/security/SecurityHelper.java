package taskmanager.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import taskmanager.exception.AccessDeniedException;
import taskmanager.model.Task;

/**
 * Centralises Spring Security context queries used across the service layer.
 *
 * <p>Keeping these concerns here means {@link taskmanager.service.TaskService}
 * has no direct dependency on {@link SecurityContextHolder}, making the service
 * easier to test and reason about independently of the security stack.
 */
@Component
public class SecurityHelper {

    /**
     * Resolves the username of the currently authenticated principal.
     *
     * @return the username string
     */
    public String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return principal.toString();
    }

    /**
     * Returns {@code true} if the currently authenticated user has the
     * {@code ROLE_ADMIN} authority.
     *
     * @return {@code true} for admins, {@code false} otherwise
     */
    public boolean isAdmin() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * Throws {@link AccessDeniedException} if the current user is neither the
     * owner of the given task nor an admin.
     *
     * @param task the task whose ownership is checked
     * @throws AccessDeniedException if access should be denied
     */
    public void checkOwnership(Task task) {
        if (!isAdmin() && !task.getUser().getUsername().equals(getCurrentUsername())) {
            throw new AccessDeniedException();
        }
    }
}
