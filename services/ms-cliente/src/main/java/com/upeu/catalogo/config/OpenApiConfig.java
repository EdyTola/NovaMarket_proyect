package com.upeu.catalogo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogoOpenApi() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:18080").description("Gateway DEV"),
                        new Server().url("http://localhost:28082").description("Gateway PROD Docker"),
                        new Server().url("http://localhost:8081").description("Microservicio directo")))
                .info(new Info()
                        .title("catalogo API")
                        .description("API REST del microservicio de gestión de categorías. Versión actual: v1")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo catalogo")
                                .email("catalogo@upeu.edu.pe"))
                        .license(new License()
                                .name("Internal Use Only")
                                .url("https://upeu.edu.pe")));
    }
}
