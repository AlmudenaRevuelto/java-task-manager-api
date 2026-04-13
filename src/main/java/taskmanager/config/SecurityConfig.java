package taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import taskmanager.security.JwtAuthFilter;

/**
 * Spring Security configuration for the Task Manager API.
 *
 * <p>Supports two authentication mechanisms:
 * <ul>
 *   <li><b>JWT Bearer</b> — token obtained from {@code POST /auth/login} or
 *       {@code POST /auth/register}. Validated by {@link JwtAuthFilter} on every
 *       request before Spring's standard filter chain.</li>
 *   <li><b>HTTP Basic</b> — username and password sent on every request (kept for
 *       Swagger UI convenience).</li>
 * </ul>
 *
 * <p>Public endpoints: {@code /auth/register}, {@code /auth/login}, Swagger UI
 * and OpenAPI docs.
 *
 * <p>This configuration is inactive in the {@code test} profile; for tests
 * see {@code TestSecurityConfig}.
 *
 * <p>User authentication is delegated to {@link taskmanager.service.CustomUserDetailsService},
 * which loads users from the database. Passwords must be stored BCrypt-encoded.
 */
@Configuration
@Profile("!test")
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Defines the security filter chain.
     *
     * <ul>
     *   <li>CSRF disabled — not needed for stateless REST APIs.</li>
     *   <li>Session management set to {@code STATELESS} — no HTTP session is created;
     *       every request must carry a valid credential (JWT Bearer or HTTP Basic).</li>
     *   <li>Swagger endpoints ({@code /swagger-ui/**}, {@code /v3/api-docs/**}) are public.</li>
     *   <li>Auth endpoints ({@code /auth/register}, {@code /auth/login}) are public.</li>
     *   <li>All other requests require authentication.</li>
     *   <li>{@link JwtAuthFilter} runs before {@code UsernamePasswordAuthenticationFilter}.</li>
     * </ul>
     *
     * @param http the {@link HttpSecurity} to configure
     * @return the built {@link SecurityFilterChain}
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/auth/register",
                    "/auth/login"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(org.springframework.security.config.Customizer.withDefaults())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Prevents Spring Boot from auto-registering {@link JwtAuthFilter} as a standard
     * servlet filter. It is already registered inside the security chain via
     * {@code addFilterBefore}, so auto-registration would cause it to run twice.
     *
     * @param filter the filter bean
     * @return a disabled {@link FilterRegistrationBean}
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Exposes the {@link AuthenticationManager} as a Spring bean so it can
     * be injected into controllers or services that need to authenticate
     * credentials programmatically (e.g. the login endpoint).
     *
     * @param config the auto-configured {@link AuthenticationConfiguration}
     * @return the default {@link AuthenticationManager}
     * @throws Exception if the manager cannot be retrieved
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}