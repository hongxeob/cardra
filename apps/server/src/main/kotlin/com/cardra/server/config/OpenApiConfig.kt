package com.cardra.server.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun cardraOpenApi(): OpenAPI {
        return OpenAPI().info(
            Info()
                .title("Cardra Server API")
                .description("Cardra backend API documentation")
                .version("v1")
                .contact(
                    Contact()
                        .name("Cardra Team"),
                )
                .license(
                    License()
                        .name("Internal Use"),
                ),
        )
    }
}
