package com.rooonnie.ecomm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ecommOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ecomm-api")
                        .description("SMD electronics e-commerce REST API")
                        .version("v1"));
    }
}
