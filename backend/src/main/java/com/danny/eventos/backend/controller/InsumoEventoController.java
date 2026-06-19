package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.dto.InsumoEventoRequestDTO;
import com.danny.eventos.backend.dto.InsumoEventoResponseDTO;
import com.danny.eventos.backend.exception.ErrorResponse;
import com.danny.eventos.backend.service.InsumoEventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eventos/{eventoId}/insumos")
@Tag(name = "Insumos", description = "Insumos necessários para a realização dos eventos")
public class InsumoEventoController {

    private final InsumoEventoService service;

    public InsumoEventoController(InsumoEventoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar insumos", description = "Lista os insumos vinculados ao evento e seus responsáveis.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumos retornados"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<InsumoEventoResponseDTO> listar(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId
    ) {
        return service.listarPorEvento(eventoId);
    }

    @PostMapping
    @Operation(summary = "Adicionar insumo", description = "Adiciona um novo insumo ao evento.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Insumo adicionado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<InsumoEventoResponseDTO> adicionar(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId,
            @Valid @RequestBody InsumoEventoRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.adicionarInsumo(eventoId, request));
    }

    @PutMapping("/{insumoId}")
    @Operation(summary = "Atualizar insumo", description = "Atualiza os dados de um insumo existente.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Insumo atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento ou insumo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public InsumoEventoResponseDTO atualizar(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId,
            @Parameter(description = "Identificador do insumo", example = "10") @PathVariable Long insumoId,
            @Valid @RequestBody InsumoEventoRequestDTO request
    ) {
        return service.atualizarInsumo(eventoId, insumoId, request);
    }

    @DeleteMapping("/{insumoId}")
    @Operation(summary = "Remover insumo", description = "Remove um insumo do evento.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Insumo removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento ou insumo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> remover(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId,
            @Parameter(description = "Identificador do insumo", example = "10") @PathVariable Long insumoId
    ) {
        service.removerInsumo(eventoId, insumoId);
        return ResponseEntity.noContent().build();
    }
}
