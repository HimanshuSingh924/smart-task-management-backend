package com.taskmanager.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3 / Swagger configuration.
 * Adds Bearer token authentication to all protected endpoints in Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.yourdomain.com").description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .name(BEARER_AUTH)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your JWT token (without 'Bearer ' prefix)")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Smart Task Management API")
                .description(
                    "Production-grade REST API for a task management platform. " +
                    "Features JWT authentication via custom jwt-auth-starter, " +
                    "MongoDB persistence, and role-based access control.\n\n" +
                    "**Authentication:** Use POST /api/v1/auth/login to obtain a JWT token, " +
                    "then click 'Authorize' above and enter the token."
                )
                .version("1.0.0")
                .contact(new Contact()
                        .name("Himanshu Singh")
                        .email("himanshu@taskmanager.com")
                        .url("https://github.com/HimanshuSingh924"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
