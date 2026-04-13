package taskmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Enables Spring Security method-level security across all profiles.
 *
 * <p>Activating {@link EnableMethodSecurity} here (rather than inside
 * {@link SecurityConfig}) ensures that {@code @PreAuthorize} and
 * {@code @PostAuthorize} annotations are enforced in every environment,
 * including the {@code test} profile, so integration tests validate the
 * same security rules as production.
 *
 * <p>This class intentionally carries no {@code @Profile} restriction.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
