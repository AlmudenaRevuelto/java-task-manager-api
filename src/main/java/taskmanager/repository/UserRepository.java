package taskmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import taskmanager.model.User;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository},
 * plus a lookup method by username used during authentication.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByUsername(String username);
}