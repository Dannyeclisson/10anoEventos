package com.danny.eventos.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "Dados públicos do usuário. Não contém senha nem senhaHash.")
public class UsuarioResponseDTO {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "Maria da Silva")
    private String nome;
    @Schema(example = "maria@email.com")
    private String email;
    @Schema(example = "1992-05-14", type = "string", format = "date")
    private LocalDate dataNascimento;
    @Schema(description = "CPF normalizado, somente números", example = "52998224725")
    private String cpf;
    @Schema(description = "Telefone normalizado, somente números", example = "11987654321")
    private String telefone;
}
