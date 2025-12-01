package com.springneobank.auth.swagger;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.PathItem;

@Configuration
public class SwaggerConfig {

    @Value("${keycloak.realm}")
    private String realmName;

    /**
     * Custom definition
     * Insert Authorization button
     * 
     * @return
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Auth API").version("v1"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addServersItem(new io.swagger.v3.oas.models.servers.Server()
                        .url("/")
                        .description("Self API Server"));
    }

    @Bean
    public OpenApiCustomizer globalSecurityCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation ->
                        operation.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("bearerAuth"))
                )
        );
    }

    @Bean
    public OpenApiCustomizer keycloakLoginCustomizer(@Value("${keycloak.realm}") String realmName) {

        String path = "/realms/" + realmName + "/protocol/openid-connect/token";

        return openApi -> {

            PathItem loginPath = new PathItem().post(
                    new Operation()
                        .addTagsItem("1- Keycloak Login") // <--- Esto es lo importante
                        .summary("Login Keycloak")
                        .description("Get JWT token from Keycloak")
                        .requestBody(new RequestBody()
                                .content(new Content()
                                        .addMediaType("application/x-www-form-urlencoded",
                                                new MediaType().schema(
                                                        new Schema<>().type("object")
                                                                .addProperty("username", new Schema<>().type("string"))
                                                                .addProperty("password", new Schema<>().type("string"))
                                                                .addProperty("client_id", new Schema<>().type("string").example("front-app"))
                                                                .addProperty("grant_type", new Schema<>().type("string").example("password"))
                                                                .addProperty("scope", new Schema<>().type("string").example("openid profile email"))
                                                ))))
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse().description("Token generated successfully"))
                                .addApiResponse("400", new ApiResponse().description("Login error"))
                        )
            );

            openApi.path(path, loginPath);
        };
    }

}
