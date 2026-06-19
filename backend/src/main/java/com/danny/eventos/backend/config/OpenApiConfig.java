package com.danny.eventos.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String COOKIE_AUTH = "cookieAuth";

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.auth.cookie-name}") String cookieName) {
        return new OpenAPI()
                .info(new Info()
                        .title("10anoEventos API")
                        .description("API para gerenciamento de eventos, usuários, inscrições, participações e insumos.")
                        .version("v1"))
                .tags(List.of(
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("Autenticação")
                                .description("Login, consulta da sessão atual e logout"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("Usuários")
                                .description("Cadastro e consulta de usuários"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("Eventos")
                                .description("Criação, consulta, edição e cancelamento de eventos"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("Participações / Inscrições")
                                .description("Vínculos entre usuários e eventos"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("Insumos")
                                .description("Insumos necessários para a realização dos eventos"),
                        new io.swagger.v3.oas.models.tags.Tag()
                                .name("Testes")
                                .description("Endpoint simples para diagnóstico da API")
                ))
                .components(new Components().addSecuritySchemes(
                        COOKIE_AUTH,
                        new SecurityScheme()
                                .name(cookieName)
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .description("Cookie HttpOnly contendo o JWT. Faça login em /api/auth/login para iniciar a sessão.")
                ));
    }

    @Bean
    public OpenApiCustomizer unexpectedErrorResponseCustomizer() {
        return openApi -> openApi.getPaths().values().stream()
                .flatMap(path -> path.readOperations().stream())
                .forEach(operation -> operation.getResponses().addApiResponse(
                        "500",
                        new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description("Erro interno inesperado")
                                .content(new io.swagger.v3.oas.models.media.Content().addMediaType(
                                        "application/json",
                                        new io.swagger.v3.oas.models.media.MediaType().schema(
                                                new io.swagger.v3.oas.models.media.Schema<>()
                                                        .$ref("#/components/schemas/ErrorResponse")
                                        )
                                ))
                ));
    }
}
