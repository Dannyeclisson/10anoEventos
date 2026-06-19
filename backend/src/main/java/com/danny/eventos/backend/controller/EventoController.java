package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.dto.CancelarEventoRequestDTO;
import com.danny.eventos.backend.dto.EventoRequest;
import com.danny.eventos.backend.dto.EventoResponse;
import com.danny.eventos.backend.exception.ErrorResponse;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
@Tag(name = "Eventos", description = "Criação, consulta, edição e cancelamento de eventos")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            summary = "Criar evento",
            description = "Cria um evento, define os status iniciais, registra o usuário autenticado como organizador e aceita insumos no payload."
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento criado"),
            @ApiResponse(responseCode = "400", description = "Dados ou datas inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public EventoResponse criar(
            @Valid @RequestBody EventoRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        return service.salvar(request, usuarioAutenticado.getId());
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Lista os eventos com seus status e quantidade de participantes ativos.")
    @ApiResponse(responseCode = "200", description = "Eventos retornados")
    public List<EventoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar detalhes do evento", description = "Retorna evento, organizador, status, participantes e insumos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public EventoResponse buscarPorId(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long id
    ) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/editar")
    @Operation(summary = "Carregar evento para edição", description = "Retorna os dados editáveis somente ao organizador do evento.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados para edição retornados"),
            @ApiResponse(responseCode = "400", description = "Evento cancelado ou finalizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é o organizador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public EventoResponse buscarParaEdicao(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        return service.buscarParaEdicao(id, usuarioAutenticado.getId());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar evento",
            description = "Atualiza o evento somente para o organizador. Alterações de data ou horário definem o status como adiado."
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou evento não editável",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é o organizador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public EventoResponse atualizar(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long id,
            @Valid @RequestBody EventoRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        return service.atualizar(id, request, usuarioAutenticado.getId());
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(
            summary = "Cancelar evento",
            description = "Cancela um evento do organizador autenticado e encerra as inscrições. Eventos finalizados não podem ser cancelados."
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento cancelado"),
            @ApiResponse(responseCode = "400", description = "Evento já cancelado ou finalizado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é o organizador",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public EventoResponse cancelar(
            @Parameter(description = "Identificador do evento", example = "1") @PathVariable Long id,
            @Valid @RequestBody CancelarEventoRequestDTO request,
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuarioAutenticado
    ) {
        return service.cancelar(id, request, usuarioAutenticado.getId());
    }
}
