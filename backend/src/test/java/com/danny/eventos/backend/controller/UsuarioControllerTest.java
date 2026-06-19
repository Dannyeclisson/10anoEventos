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
                  "cpf": "529.982.247-25",
                  "telefone": "(11) 98765-4321"
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
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.telefone").value("11987654321"))
                .andExpect(jsonPath("$.tipo").doesNotExist())
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
                .andExpect(jsonPath("$.tipo").doesNotExist())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    @Test
    void deveRejeitarCpfInvalido() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "CPF Invalido",
                                  "email": "cpf.invalido@example.com",
                                  "senha": "SenhaTeste123",
                                  "dataNascimento": "1992-05-14",
                                  "cpf": "111.111.111-11",
                                  "telefone": "11981112222"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("CPF inválido"));
    }

    @Test
    void deveRejeitarTelefoneDuplicadoMesmoComMascarasDiferentes() throws Exception {
        cadastrarUsuarioParaTeste(
                "Telefone Um",
                "telefone.um@example.com",
                "168.995.350-09",
                "(21) 98888-7777"
        );

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Telefone Dois",
                                  "email": "telefone.dois@example.com",
                                  "senha": "SenhaTeste123",
                                  "dataNascimento": "1992-05-14",
                                  "cpf": "862.883.667-57",
                                  "telefone": "21988887777"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Telefone já cadastrado"));
    }

    private void cadastrarUsuarioParaTeste(String nome, String email, String cpf, String telefone) throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "%s",
                                  "email": "%s",
                                  "senha": "SenhaTeste123",
                                  "dataNascimento": "1992-05-14",
                                  "cpf": "%s",
                                  "telefone": "%s"
                                }
                                """.formatted(nome, email, cpf, telefone)))
                .andExpect(status().isCreated());
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
