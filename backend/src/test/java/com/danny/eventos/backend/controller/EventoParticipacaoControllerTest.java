package com.danny.eventos.backend.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EventoParticipacaoControllerTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRegistrarOrganizadorEPermitirSomenteParticipacaoManualValida() throws Exception {
        long organizadorId = cadastrarUsuario(
                "Organizador Evento",
                "organizador.evento@example.com",
                "935.411.347-80",
                "(11) 91111-2222"
        );
        long participanteId = cadastrarUsuario(
                "Pessoa Colaboradora",
                "colaborador.evento@example.com",
                "987.654.321-00",
                "(11) 93333-4444"
        );
        Cookie authCookie = login("organizador.evento@example.com", "SenhaTeste123");
        Cookie participanteCookie = login("colaborador.evento@example.com", "SenhaTeste123");

        long eventoId = criarEvento(organizadorId, authCookie);
        long insumoId = criarInsumo(eventoId, authCookie);

        mockMvc.perform(get("/api/eventos/" + eventoId + "/editar").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventoId));

        mockMvc.perform(get("/api/eventos/" + eventoId + "/editar").cookie(participanteCookie))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/eventos/999999/editar").cookie(authCookie))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/eventos/" + eventoId + "/participacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioId").value(organizadorId))
                .andExpect(jsonPath("$[0].tipoRelacao").value(3))
                .andExpect(jsonPath("$[0].descricaoTipoRelacao").value("ORGANIZADOR"));

        mockMvc.perform(post("/api/eventos/" + eventoId + "/participacoes")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": %d,
                                  "tipoRelacao": 2
                                }
                                """.formatted(participanteId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/eventos/" + eventoId + "/participacoes")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": %d,
                                  "tipoRelacao": 2,
                                  "insumoIds": [%d]
                                }
                                """.formatted(participanteId, insumoId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(participanteId))
                .andExpect(jsonPath("$.tipoRelacao").value(2))
                .andExpect(jsonPath("$.descricaoTipoRelacao").value("COLABORADOR"));

        mockMvc.perform(get("/api/eventos/" + eventoId + "/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].responsavelId").value(participanteId))
                .andExpect(jsonPath("$[0].status").value("CONFIRMADO"));

        mockMvc.perform(get("/api/eventos/" + eventoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantes").value(1))
                .andExpect(jsonPath("$.statusInscricao").value("aberta"));

        mockMvc.perform(post("/api/eventos/" + eventoId + "/participacoes")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "usuarioId": %d,
                                  "tipoRelacao": 1
                                }
                                """.formatted(participanteId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/eventos/" + eventoId + "/participacoes/" + organizadorId)
                        .cookie(authCookie))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/eventos/" + eventoId + "/cancelar")
                        .cookie(participanteCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/eventos/" + eventoId + "/cancelar")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "motivo": "Cancelamento de teste" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusEvento").value("cancelado"))
                .andExpect(jsonPath("$.statusInscricao").value("encerrada"));

        mockMvc.perform(get("/api/eventos/" + eventoId + "/editar").cookie(authCookie))
                .andExpect(status().isBadRequest());
    }

    private long cadastrarUsuario(String nome, String email, String cpf, String telefone) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "email": "%s",
                                  "senha": "SenhaTeste123",
                                  "dataNascimento": "1990-01-10",
                                  "cpf": "%s",
                                  "telefone": "%s"
                                }
                                """.formatted(nome, email, cpf, telefone)))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private long criarInsumo(long eventoId, Cookie authCookie) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/eventos/" + eventoId + "/insumos")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoria": "Bebidas",
                                  "nome": "Agua mineral",
                                  "quantidade": 100,
                                  "unidadeMedida": "garrafas",
                                  "observacoes": "500ml",
                                  "status": "PENDENTE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private Cookie login(String email, String senha) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "senha": "%s"
                                }
                                """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("AUTH_TOKEN");
    }

    private long criarEvento(long organizadorId, Cookie authCookie) throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        LocalDateTime fim = inicio.plusHours(3);
        LocalDateTime inicioInscricoes = LocalDateTime.now();

        MvcResult result = mockMvc.perform(post("/api/eventos")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Evento de integracao",
                                  "descricao": "Encontro para validar participacoes",
                                  "local": "Serpro",
                                  "dataInicio": "%s",
                                  "dataFim": "%s",
                                  "dataInicioInscricoes": "%s",
                                  "capacidadeParticipantes": 20,
                                  "organizadorId": %d
                                }
                                """.formatted(inicio, fim, inicioInscricoes, organizadorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantes").value(0))
                .andExpect(jsonPath("$.statusEvento").value("agendado"))
                .andReturn();

        return extractId(result);
    }

    private long extractId(MvcResult result) throws Exception {
        Matcher matcher = ID_PATTERN.matcher(result.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new AssertionError("Resposta sem id");
        }

        return Long.parseLong(matcher.group(1));
    }
}
