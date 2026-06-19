package com.danny.eventos.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void devePublicarOpenApiCompletaSemExporSenhaNosResponses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.info.title").value("10anoEventos API"))
                .andExpect(jsonPath("$.info.version").value("v1"))
                .andExpect(jsonPath("$.tags[*].name", hasItems(
                        "Autenticação",
                        "Usuários",
                        "Eventos",
                        "Participações / Inscrições",
                        "Insumos",
                        "Testes"
                )))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth").exists())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.tags[0]").value("Autenticação"))
                .andExpect(jsonPath("$.paths['/api/usuarios'].post.tags[0]").value("Usuários"))
                .andExpect(jsonPath("$.paths['/api/eventos/{id}/editar'].get.tags[0]").value("Eventos"))
                .andExpect(jsonPath("$.paths['/api/eventos/{eventoId}/participacoes'].post.tags[0]")
                        .value("Participações / Inscrições"))
                .andExpect(jsonPath("$.paths['/api/eventos/{eventoId}/insumos'].get.tags[0]").value("Insumos"))
                .andExpect(jsonPath("$.paths['/api/test'].get.tags[0]").value("Testes"))
                .andExpect(jsonPath("$.paths['/api/eventos'].get.responses['500']").exists())
                .andExpect(jsonPath("$.components.schemas.UsuarioResponseDTO.properties.senha").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.UsuarioResponseDTO.properties.senhaHash").doesNotExist());
    }

    @Test
    void devePermitirAcessoPublicoAoSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }
}
