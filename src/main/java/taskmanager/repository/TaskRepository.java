package taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import taskmanager.model.Task;

import java.util.Optional;

public interface TaskRepository
        extends JpaRepository<Task, Long>,
                JpaSpecificationExecutor<Task> {

    /**
     * Finds a task by its ID and the username of its owner.
     * Returns empty if the task does not exist or belongs to a different user.
     *
     * @param id       the task ID
     * @param username the owner's username
     * @return an {@link Optional} containing the task, or empty
     */
    Optional<Task> findByIdAndUserUsername(Long id, String username);
}