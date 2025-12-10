package org.pacos.core.config.swagger;

import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pacosOpenAPI() {
        Server server = new Server();
        server.setUrl("/");
        server.setDescription("Current instance");
        return new OpenAPI()
                .info(new Info().title("PacOS API").version("1.0"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("apiKeyAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Enter API token")
                        )
                )
                .servers(List.of(server))
                .addSecurityItem(new SecurityRequirement().addList("apiKeyAuth"));
    }

}
