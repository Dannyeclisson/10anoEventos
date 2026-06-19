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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InsumoEventoControllerTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarListarAtualizarERemoverInsumosDoEvento() throws Exception {
        long organizadorId = cadastrarUsuario();
        Cookie authCookie = login();
        long eventoId = criarEventoComInsumos(organizadorId, authCookie);

        MvcResult listagemInicial = mockMvc.perform(get("/api/eventos/" + eventoId + "/insumos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Bebidas"))
                .andExpect(jsonPath("$[0].nome").value("Agua mineral"))
                .andExpect(jsonPath("$[1].categoria").value("Mobiliario"))
                .andExpect(jsonPath("$[1].nome").value("Cadeiras"))
                .andReturn();

        long primeiroInsumoId = extractId(listagemInicial);

        mockMvc.perform(post("/api/eventos/" + eventoId + "/insumos")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoria": "Limpeza",
                                  "nome": "Sacos de lixo",
                                  "quantidade": 20,
                                  "unidadeMedida": "unidades",
                                  "observacoes": "Reforcados",
                                  "status": "PENDENTE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoria").value("Limpeza"))
                .andExpect(jsonPath("$.nome").value("Sacos de lixo"));

        mockMvc.perform(put("/api/eventos/" + eventoId + "/insumos/" + primeiroInsumoId)
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoria": "Bebidas",
                                  "nome": "Agua mineral sem gas",
                                  "quantidade": 120,
                                  "unidadeMedida": "garrafas",
                                  "observacoes": "500ml",
                                  "status": "CONFIRMADO"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Agua mineral sem gas"))
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        mockMvc.perform(delete("/api/eventos/" + eventoId + "/insumos/" + primeiroInsumoId)
                        .cookie(authCookie))
                .andExpect(status().isNoContent());
    }

    private long cadastrarUsuario() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Organizador Insumos",
                                  "email": "organizador.insumos@example.com",
                                  "senha": "SenhaTeste123",
                                  "dataNascimento": "1990-01-10",
                                  "cpf": "123.456.789-09",
                                  "telefone": "(11) 95555-6666"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return extractId(result);
    }

    private Cookie login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "organizador.insumos@example.com",
                                  "senha": "SenhaTeste123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("AUTH_TOKEN");
    }

    private long criarEventoComInsumos(long organizadorId, Cookie authCookie) throws Exception {
        LocalDateTime inicio = LocalDateTime.now().plusDays(10);
        LocalDateTime fim = inicio.plusHours(3);
        LocalDateTime inicioInscricoes = LocalDateTime.now();

        MvcResult result = mockMvc.perform(post("/api/eventos")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Evento com insumos",
                                  "descricao": "Evento para validar lista de insumos",
                                  "local": "Serpro",
                                  "dataInicio": "%s",
                                  "dataFim": "%s",
                                  "dataInicioInscricoes": "%s",
                                  "capacidadeParticipantes": 20,
                                  "organizadorId": %d,
                                  "insumos": [
                                    {
                                      "categoria": "Bebidas",
                                      "nome": "Agua mineral",
                                      "quantidade": 100,
                                      "unidadeMedida": "garrafas",
                                      "observacoes": "500ml",
                                      "status": "PENDENTE"
                                    },
                                    {
                                      "categoria": "Mobiliario",
                                      "nome": "Cadeiras",
                                      "quantidade": 120,
                                      "unidadeMedida": "unidades",
                                      "observacoes": "Plasticas brancas",
                                      "status": "PENDENTE"
                                    }
                                  ]
                                }
                                """.formatted(inicio, fim, inicioInscricoes, organizadorId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insumos[0].nome").value("Agua mineral"))
                .andExpect(jsonPath("$.insumos[1].nome").value("Cadeiras"))
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
