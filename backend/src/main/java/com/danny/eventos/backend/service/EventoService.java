package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.EventoRequest;
import com.danny.eventos.backend.dto.EventoResponse;
import com.danny.eventos.backend.exception.ResourceNotFoundException;
import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.repository.EventoRepository;
import com.danny.eventos.backend.repository.InsumoEventoRepository;
import com.danny.eventos.backend.repository.UsuarioEventoRepository;
import com.danny.eventos.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioEventoRepository usuarioEventoRepository;
    private final UsuarioEventoService usuarioEventoService;
    private final InsumoEventoRepository insumoEventoRepository;
    private final InsumoEventoService insumoEventoService;

    public EventoService(
            EventoRepository repository,
            UsuarioRepository usuarioRepository,
            UsuarioEventoRepository usuarioEventoRepository,
            UsuarioEventoService usuarioEventoService,
            InsumoEventoRepository insumoEventoRepository,
            InsumoEventoService insumoEventoService
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioEventoRepository = usuarioEventoRepository;
        this.usuarioEventoService = usuarioEventoService;
        this.insumoEventoRepository = insumoEventoRepository;
        this.insumoEventoService = insumoEventoService;
    }

    private EventoResponse toResponse(Evento evento) {
        return EventoResponse.builder()
                .id(evento.getId())
                .nome(evento.getNome())
                .descricao(evento.getDescricao())
                .local(evento.getLocal())
                .dataHora(evento.getDataHora().toString())
                .organizadorId(evento.getOrganizador().getId())
                .organizadorNome(evento.getOrganizador().getNome())
                .participantes(usuarioEventoRepository.countByEventoId(evento.getId()))
                .insumos(insumoEventoRepository.findByEventoId(evento.getId())
                        .stream()
                        .map(insumoEventoService::toResponse)
                        .toList())
                .build();
    }

    @Transactional
    public EventoResponse salvar(EventoRequest request) {
        Usuario organizador = usuarioRepository.findById(request.getOrganizadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        Evento evento = Evento.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .local(request.getLocal())
                .dataHora(LocalDateTime.parse(request.getDataHora()))
                .organizador(organizador)
                .build();

        Evento salvo = repository.save(evento);
        usuarioEventoService.registrarOrganizador(salvo, organizador);
        insumoEventoService.salvarInsumos(salvo, request.getInsumos());

        return toResponse(salvo);
    }

    public List<EventoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public EventoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Evento nao encontrado"));
    }
}
