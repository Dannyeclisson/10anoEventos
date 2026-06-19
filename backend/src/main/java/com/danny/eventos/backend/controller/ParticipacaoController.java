package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.dto.CancelarInscricaoRequestDTO;
import com.danny.eventos.backend.dto.ParticiparEventoRequestDTO;
import com.danny.eventos.backend.dto.UsuarioEventoResponseDTO;
import com.danny.eventos.backend.exception.ErrorResponse;
import com.danny.eventos.backend.service.UsuarioEventoService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Participações / Inscrições", description = "Vínculos entre usuários e eventos")
public class ParticipacaoController {

    private final UsuarioEventoService service;

    public ParticipacaoController(UsuarioEventoService service) {
        this.service = service;
    }

    @PostMapping("/api/eventos/{eventoId}/participacoes")
    @Operation(
            summary = "Confirmar participação",
            description = "Vincula um usuário como participante ou colaborador. O tipo organizador não pode ser definido por este fluxo."
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Participação confirmada"),
            @ApiResponse(responseCode = "400", description = "Inscrição inválida, indisponível ou já existente",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento ou usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioEventoResponseDTO> participar(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId,
            @Valid @RequestBody ParticiparEventoRequestDTO request
    ) {
        UsuarioEventoResponseDTO relacao = service.participarEvento(eventoId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(relacao);
    }

    @GetMapping("/api/eventos/{eventoId}/participacoes")
    @Operation(summary = "Listar participações do evento", description = "Lista usuários vinculados ao evento e o tipo de relação de cada um.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participações retornadas"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<UsuarioEventoResponseDTO> listarParticipacoes(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId
    ) {
        return service.listarRelacoesPorEvento(eventoId);
    }

    @GetMapping("/api/usuarios/{usuarioId}/eventos")
    @Operation(summary = "Listar eventos do usuário", description = "Lista todos os vínculos do usuário com eventos.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vínculos retornados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<UsuarioEventoResponseDTO> listarEventosDoUsuario(
            @Parameter(description = "Identificador do usuário", example = "1") @PathVariable Long usuarioId
    ) {
        return service.listarRelacoesPorUsuario(usuarioId);
    }

    @PatchMapping("/api/eventos/{eventoId}/inscricao/cancelar")
    @Operation(
            summary = "Cancelar inscrição",
            description = "Marca a relação como cancelada e libera insumos sob responsabilidade do colaborador, quando aplicável."
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscrição cancelada"),
            @ApiResponse(responseCode = "400", description = "Inscrição não pode ser cancelada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento, usuário ou inscrição não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioEventoResponseDTO cancelarInscricao(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId,
            @Valid @RequestBody CancelarInscricaoRequestDTO request
    ) {
        return service.cancelarInscricao(eventoId, request);
    }

    @DeleteMapping("/api/eventos/{eventoId}/participacoes/{usuarioId}")
    @Operation(summary = "Remover participação", description = "Remove ou cancela o vínculo de um usuário com o evento.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Participação removida"),
            @ApiResponse(responseCode = "400", description = "Vínculo não pode ser removido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Vínculo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> removerParticipacao(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long eventoId,
            @Parameter(description = "Identificador do usuário", example = "2") @PathVariable Long usuarioId
    ) {
        service.removerRelacao(eventoId, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
