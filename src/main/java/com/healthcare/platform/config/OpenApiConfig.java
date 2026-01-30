package com.healthcare.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration.
 * Provides API documentation with JWT authentication support.
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare Platform API")
                        .version("1.0.0")
                        .description("""
                                Healthcare Platform Starter Kit API documentation.
                                
                                This API provides:
                                - Patient management (CRUD operations)
                                - JWT authentication
                                - Health and readiness endpoints
                                
                                ## Authentication
                                
                                Use the `/api/v1/auth/login` endpoint to obtain a JWT token.
                                
                                **Demo Credentials:**
                                - admin / admin123 (ADMIN, USER roles)
                                - user / user123 (USER role)
                                - doctor / doctor123 (DOCTOR, USER roles)
                                - nurse / nurse123 (NURSE, USER roles)
                                
                                Include the token in the Authorization header:
                                ```
                                Authorization: Bearer <your-token>
                                ```
                                """)
                        .contact(new Contact()
                                .name("Healthcare Platform Team")
                                .email("support@healthcare-platform.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local development server")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
