package com.danny.eventos.backend.dto;

import com.danny.eventos.backend.validation.CPFValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Dados para cadastro de usuário")
public class UsuarioCadastroDTO {

    @Schema(description = "Nome completo", example = "Maria da Silva")
    @NotBlank(message = "Nome e obrigatorio")
    private String nome;

    @Schema(description = "Email único e válido", example = "maria@email.com")
    @NotBlank(message = "Email e obrigatorio")
    @Email(message = "Email invalido")
    private String email;

    @Schema(description = "Senha com pelo menos oito caracteres", example = "SenhaTeste123", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "Senha e obrigatoria")
    @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres")
    private String senha;

    @Schema(description = "Data de nascimento no passado", example = "1992-05-14", type = "string", format = "date")
    @NotNull(message = "Data de nascimento e obrigatoria")
    @Past(message = "Data de nascimento deve estar no passado")
    private LocalDate dataNascimento;

    @Schema(description = "CPF válido, com ou sem máscara", example = "529.982.247-25")
    @NotBlank(message = "CPF e obrigatorio")
    @CPFValido
    private String cpf;

    @Schema(description = "Telefone único, com ou sem máscara", example = "(11) 98765-4321")
    @NotBlank(message = "Telefone e obrigatorio")
    private String telefone;
}
