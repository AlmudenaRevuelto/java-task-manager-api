package taskmanager.model;

/**
 * Roles available in the application.
 *
 * <ul>
 *   <li>{@link #USER} — standard authenticated user; can manage only their own tasks.</li>
 *   <li>{@link #ADMIN} — privileged user; reserved for future administrative operations.</li>
 * </ul>
 */
public enum Role {
    /** Standard user role assigned to every newly registered account. */
    USER,
    /** Administrator role reserved for privileged operations. */
    ADMIN
}
