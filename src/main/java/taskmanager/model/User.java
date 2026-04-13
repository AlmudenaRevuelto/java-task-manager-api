package taskmanager.model;

import jakarta.persistence.*;

/**
 * JPA entity representing an application user.
 *
 * <p>Stored in the {@code users} table. The username must be unique
 * and is used as the principal identifier for authentication.
 *
 * <p>Passwords must be stored encoded (e.g. BCrypt). Plain-text
 * passwords must never be persisted.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique login name for the user. Cannot be null. */
    @Column(nullable = false, unique = true)
    private String username;

    /** Encoded password. Cannot be null. */
    @Column(nullable = false)
    private String password;

    /** Required by JPA. */
    public User() {}

    /**
     * Creates a new user with the given credentials.
     *
     * @param username the unique login name
     * @param password the encoded password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the user's database ID.
     *
     * @return the ID
     */
    public Long getId() {
        return id;
    }

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
     * @param username the new username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the encoded password.
     *
     * @return the encoded password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the encoded password.
     *
     * @param password the new encoded password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}