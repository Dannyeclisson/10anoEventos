package com.danny.eventos.backend.controller;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveLogarBuscarUsuarioLogadoELogoutSemExporSenha() throws Exception {
        cadastrarUsuario();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "auth.teste@example.com",
                                  "senha": "SenhaTeste123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("AUTH_TOKEN"))
                .andExpect(jsonPath("$.email").value("auth.teste@example.com"))
                .andExpect(jsonPath("$.tipo").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();

        Cookie authCookie = login.getResponse().getCookie("AUTH_TOKEN");

        mockMvc.perform(get("/api/auth/me").cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("auth.teste@example.com"))
                .andExpect(jsonPath("$.tipo").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());

        mockMvc.perform(post("/api/auth/logout").cookie(authCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("AUTH_TOKEN", 0));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUnauthorizedParaCredenciaisInvalidas() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "nao.existe@example.com",
                                  "senha": "senhaerrada"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private void cadastrarUsuario() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Usuario Auth",
                                  "email": "auth.teste@example.com",
                                  "senha": "SenhaTeste123",
                                  "dataNascimento": "2001-11-24",
                                  "cpf": "074.885.581-54",
                                  "telefone": "(61) 98326-5978"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
