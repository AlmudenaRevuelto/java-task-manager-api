package taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the {@link PasswordEncoder} bean independently of any security profile.
 *
 * <p>Kept in a separate class so that it is available in all Spring profiles,
 * including {@code test}, where {@link SecurityConfig} is disabled via
 * {@code @Profile("!test")}. Any component that needs to encode or verify
 * passwords (e.g. {@link taskmanager.controller.AuthController}) can inject
 * this bean regardless of the active profile.
 */
@Configuration
public class EncoderConfig {

    /**
     * Returns a {@link BCryptPasswordEncoder} instance for hashing and verifying passwords.
     *
     * @return the {@link PasswordEncoder} bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
