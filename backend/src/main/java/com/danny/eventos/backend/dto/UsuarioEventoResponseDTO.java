package com.danny.eventos.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsuarioEventoResponseDTO {

    private Long id;
    private Long eventoId;
    private String nomeEvento;
    private Long usuarioId;
    private String nomeUsuario;
    private Integer tipoRelacao;
    private String descricaoTipoRelacao;
}
