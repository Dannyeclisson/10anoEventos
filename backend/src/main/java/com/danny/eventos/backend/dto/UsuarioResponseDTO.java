package com.danny.eventos.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UsuarioResponseDTO {

    private Long id;
    private String nome;
    private String email;
    private LocalDate dataNascimento;
    private String cpf;
    private String telefone;
}
