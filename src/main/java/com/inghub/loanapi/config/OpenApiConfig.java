package com.inghub.loanapi.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan API")
                        .version("1.0")
                        .description("API documentation with JWT authentication."))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth")) // Apply the security requirement globally
                ;
    }

    private io.swagger.v3.oas.models.security.SecurityScheme securityScheme(String name) {
        return new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY)
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                .name(name);
    }
}



