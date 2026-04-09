package taskmanager.specification;

import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import taskmanager.model.Priority;
import taskmanager.model.Task;

/**
 * JPA Specification factory for dynamically composing {@link Task} query predicates.
 * Each method returns a {@link Specification} that can be combined with others using
 * {@link Specification#and} or {@link Specification#where}.
 */
public class TaskSpecification {

    /**
     * Filters tasks by their completion status.
     *
     * @param completed {@code true} for completed tasks, {@code false} for pending;
     *                  {@code null} disables this filter
     * @return a {@link Specification} predicate, or a no-op if {@code completed} is null
     */
    public static Specification<Task> hasCompleted(Boolean completed) {
        return (root, query, cb) ->
                completed == null ? null : cb.equal(root.get("completed"), completed);
    }

    /**
     * Filters tasks by priority level.
     *
     * @param priority the priority to match; {@code null} disables this filter
     * @return a {@link Specification} predicate, or a no-op if {@code priority} is null
     */
    public static Specification<Task> hasPriority(Priority priority) {
        return (root, query, cb) ->
                priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    /**
     * Filters tasks whose due date is on or before the given date.
     *
     * @param dueBefore the upper bound date (inclusive); {@code null} disables this filter
     * @return a {@link Specification} predicate, or a no-op if {@code dueBefore} is null
     */
    public static Specification<Task> dueBefore(LocalDate dueBefore) {
        return (root, query, cb) ->
                dueBefore == null ? null : cb.lessThanOrEqualTo(root.get("dueDate"), dueBefore);
    }

    /**
     * Filters tasks whose title or description contains the given text (case-insensitive).
     *
     * @param search the substring to search for; {@code null} or blank disables this filter
     * @return a {@link Specification} predicate, or a no-op if {@code search} is blank
     */
    public static Specification<Task> containsText(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }
}



