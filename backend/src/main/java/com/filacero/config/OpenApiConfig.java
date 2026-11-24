package com.filacero.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación Swagger en /swagger-ui.html con botón Authorize para el token JWT. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI documentacion() {
        return new OpenAPI()
                .info(new Info()
                        .title("Fila Cero API")
                        .version("1.0.0")
                        .description("Gestión de turnos para las sucursales del Banco del Galeras (proyecto académico, banco ficticio). "
                                + "Inicia sesión en POST /api/auth/login, copia el token y pulsa Authorize.")
                        .contact(new Contact().name("Manuel Alejandro Villarreal")))
                .addSecurityItem(new SecurityRequirement().addList("jwt"))
                .components(new Components().addSecuritySchemes("jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
