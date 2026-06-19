package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Credenciais para autenticação")
public class LoginRequestDTO {

    @Schema(description = "Email cadastrado", example = "usuario@email.com")
    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email invalido")
    private String email;

    @Schema(description = "Senha do usuário", example = "SenhaTeste123", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "Senha e obrigatoria")
    private String senha;
}
