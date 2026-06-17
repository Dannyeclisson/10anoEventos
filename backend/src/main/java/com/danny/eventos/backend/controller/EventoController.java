package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.dto.EventoRequest;
import com.danny.eventos.backend.dto.EventoResponse;
import com.danny.eventos.backend.dto.InsumoEventoRequestDTO;
import com.danny.eventos.backend.dto.InsumoEventoResponseDTO;
import com.danny.eventos.backend.dto.ParticiparEventoRequestDTO;
import com.danny.eventos.backend.dto.UsuarioEventoResponseDTO;
import com.danny.eventos.backend.service.EventoService;
import com.danny.eventos.backend.service.InsumoEventoService;
import com.danny.eventos.backend.service.UsuarioEventoService;
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
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService service;
    private final UsuarioEventoService usuarioEventoService;
    private final InsumoEventoService insumoEventoService;

    public EventoController(
            EventoService service,
            UsuarioEventoService usuarioEventoService,
            InsumoEventoService insumoEventoService
    ) {
        this.service = service;
        this.usuarioEventoService = usuarioEventoService;
        this.insumoEventoService = insumoEventoService;
    }

    @PostMapping
    public EventoResponse criar(@Valid @RequestBody EventoRequest request) {
        return service.salvar(request);
    }

    @GetMapping
    public List<EventoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public EventoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/{eventoId}/participacoes")
    public ResponseEntity<UsuarioEventoResponseDTO> participar(
            @PathVariable Long eventoId,
            @Valid @RequestBody ParticiparEventoRequestDTO request
    ) {
        UsuarioEventoResponseDTO relacao = usuarioEventoService.participarEvento(eventoId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(relacao);
    }

    @GetMapping("/{eventoId}/participacoes")
    public List<UsuarioEventoResponseDTO> listarParticipacoes(@PathVariable Long eventoId) {
        return usuarioEventoService.listarRelacoesPorEvento(eventoId);
    }

    @DeleteMapping("/{eventoId}/participacoes/{usuarioId}")
    public ResponseEntity<Void> removerParticipacao(
            @PathVariable Long eventoId,
            @PathVariable Long usuarioId
    ) {
        usuarioEventoService.removerRelacao(eventoId, usuarioId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventoId}/insumos")
    public List<InsumoEventoResponseDTO> listarInsumos(@PathVariable Long eventoId) {
        return insumoEventoService.listarPorEvento(eventoId);
    }

    @PostMapping("/{eventoId}/insumos")
    public ResponseEntity<InsumoEventoResponseDTO> adicionarInsumo(
            @PathVariable Long eventoId,
            @Valid @RequestBody InsumoEventoRequestDTO request
    ) {
        InsumoEventoResponseDTO insumo = insumoEventoService.adicionarInsumo(eventoId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(insumo);
    }

    @PutMapping("/{eventoId}/insumos/{insumoId}")
    public InsumoEventoResponseDTO atualizarInsumo(
            @PathVariable Long eventoId,
            @PathVariable Long insumoId,
            @Valid @RequestBody InsumoEventoRequestDTO request
    ) {
        return insumoEventoService.atualizarInsumo(eventoId, insumoId, request);
    }

    @DeleteMapping("/{eventoId}/insumos/{insumoId}")
    public ResponseEntity<Void> removerInsumo(
            @PathVariable Long eventoId,
            @PathVariable Long insumoId
    ) {
        insumoEventoService.removerInsumo(eventoId, insumoId);

        return ResponseEntity.noContent().build();
    }
}
