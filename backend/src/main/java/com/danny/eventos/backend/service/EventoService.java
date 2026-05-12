package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.EventoRequest;
import com.danny.eventos.backend.dto.EventoResponse;
import com.danny.eventos.backend.exception.ResourceNotFoundException;
import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.repository.EventoRepository;
import com.danny.eventos.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public EventoService(EventoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
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
            .build();
    }

    public EventoResponse salvar(EventoRequest request) {
        Usuario organizador = usuarioRepository.findById(request.getOrganizadorId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Evento evento = Evento.builder()
            .nome(request.getNome())
            .descricao(request.getDescricao())
            .local(request.getLocal())
            .dataHora(LocalDateTime.parse(request.getDataHora()))
            .organizador(organizador)
            .build();

        Evento salvo = repository.save(evento);

        return toResponse(salvo);
    }

    public List<EventoResponse> listar() {
        return repository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }
}
