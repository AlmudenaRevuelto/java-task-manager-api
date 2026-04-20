package taskmanager.config;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

import taskmanager.security.JwtAuthFilter;

/**
 * Spring Security configuration for the Task Manager API.
 *
 * <p>Authentication is handled exclusively via JWT Bearer tokens obtained from
 * {@code POST /auth/login}. HTTP Basic is intentionally disabled to prevent the
 * browser from showing its native credential dialog on 401 responses.
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
     *   <li>Session management set to {@code STATELESS} — every request must carry a JWT.</li>
     *   <li>HTTP Basic disabled — avoids the browser native credential popup on 401.</li>
     *   <li>Custom entry point returns plain 401 without a {@code WWW-Authenticate} header.</li>
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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(ex -> ex
                // Return a plain 401 without a WWW-Authenticate header so the browser
                // does not show its native Basic-auth credential dialog.
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
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
    /**
     * Allows cross-origin requests from the Vite dev server ({@code localhost:5173})
     * and from the Dockerised nginx frontend ({@code localhost:80}).
     *
     * @return the CORS configuration source
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

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