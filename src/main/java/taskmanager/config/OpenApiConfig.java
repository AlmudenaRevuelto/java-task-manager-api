package taskmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger UI configuration for the Task Manager API.
 *
 * <p>Registers two global security schemes so that the Swagger UI
 * displays the "Authorize" button:
 * <ul>
 *   <li>{@code basicAuth} — HTTP Basic authentication (username + password).</li>
 *   <li>{@code bearerAuth} — JWT Bearer token obtained from {@code POST /auth/login}.</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";
    private static final String BEARER_AUTH = "bearerAuth";

    /**
     * Builds the {@link OpenAPI} descriptor with project metadata and a
     * global HTTP Basic authentication requirement.
     *
     * @return the customised {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Task Manager API")
                .version("1.0.0")
                .description("""
                    REST API for managing tasks. Supports full CRUD operations. \
                    Each task has a title, optional description, completion status, \
                    priority level (LOW, MEDIUM, HIGH) and an optional due date.
                    """)
                .contact(new Contact()
                    .name("Almudena")
                    .url("https://github.com/AlmudenaRevuelto"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
            .components(new Components()
                .addSecuritySchemes(BASIC_AUTH, new SecurityScheme()
                    .name(BASIC_AUTH)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic"))
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                    .name(BEARER_AUTH)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}