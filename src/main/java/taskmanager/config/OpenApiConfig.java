package taskmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

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
                    .url("https://opensource.org/licenses/MIT")));
    }
}