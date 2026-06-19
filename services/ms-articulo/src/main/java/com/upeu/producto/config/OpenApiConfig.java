package com.upeu.producto.config;

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
    public OpenAPI productoOpenApi() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:18080").description("Gateway DEV"),
                        new Server().url("http://localhost:28082").description("Gateway PROD Docker"),
                        new Server().url("http://localhost:9091").description("Microservicio directo")))
                .info(new Info()
                        .title("producto API")
                        .description("API REST del microservicio de gestión de productos. Versión actual: v1")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo producto")
                                .email("producto@upeu.edu.pe"))
                        .license(new License()
                                .name("Internal Use Only")
                                .url("https://upeu.edu.pe")));
    }
}
