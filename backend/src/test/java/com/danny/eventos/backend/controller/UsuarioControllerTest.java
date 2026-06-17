package com.danny.eventos.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCadastrarListarEBuscarUsuarioSemExporSenha() throws Exception {
        String payload = """
                {
                  "nome": "Marina Carvalho",
                  "email": "marina.carvalho@example.com",
                  "senha": "SenhaTeste123",
                  "dataNascimento": "1992-05-14",
                  "cpf": "123.456.789-00",
                  "telefone": "(11) 98765-4321",
                  "tipo": "PARTICIPANTE"
                }
                """;

        MvcResult cadastro = mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Marina Carvalho"))
                .andExpect(jsonPath("$.email").value("marina.carvalho@example.com"))
                .andExpect(jsonPath("$.dataNascimento").value("1992-05-14"))
                .andExpect(jsonPath("$.cpf").value("123.456.789-00"))
                .andExpect(jsonPath("$.telefone").value("(11) 98765-4321"))
                .andExpect(jsonPath("$.tipo").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();

        long usuarioId = extractId(cadastro);

        Cookie authCookie = login("marina.carvalho@example.com", "SenhaTeste123");

        mockMvc.perform(get("/api/usuarios").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senha").doesNotExist())
                .andExpect(jsonPath("$[0].senhaHash").doesNotExist());

        mockMvc.perform(get("/api/usuarios/" + usuarioId).cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("marina.carvalho@example.com"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    private Cookie login(String email, String senha) throws Exception {
        String payload = """
                {
                  "email": "%s",
                  "senha": "%s"
                }
                """.formatted(email, senha);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getCookie("AUTH_TOKEN");
    }

    private long extractId(MvcResult result) throws Exception {
        Matcher matcher = ID_PATTERN.matcher(result.getResponse().getContentAsString());
        if (!matcher.find()) {
            throw new AssertionError("Resposta de cadastro sem id");
        }

        return Long.parseLong(matcher.group(1));
    }
}
