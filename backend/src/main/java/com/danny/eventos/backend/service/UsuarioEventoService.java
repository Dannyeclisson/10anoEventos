package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.ParticiparEventoRequestDTO;
import com.danny.eventos.backend.dto.UsuarioEventoResponseDTO;
import com.danny.eventos.backend.exception.ResourceNotFoundException;
import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.model.TipoRelacaoEvento;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.model.UsuarioEvento;
import com.danny.eventos.backend.repository.EventoRepository;
import com.danny.eventos.backend.repository.UsuarioEventoRepository;
import com.danny.eventos.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioEventoService {

    private final UsuarioEventoRepository repository;
    private final EventoRepository eventoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InsumoEventoService insumoEventoService;

    public UsuarioEventoService(
            UsuarioEventoRepository repository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            InsumoEventoService insumoEventoService
    ) {
        this.repository = repository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.insumoEventoService = insumoEventoService;
    }

    @Transactional
    public UsuarioEventoResponseDTO registrarOrganizador(Evento evento, Usuario usuario) {
        if (repository.existsByEventoIdAndUsuarioId(evento.getId(), usuario.getId())) {
            throw new IllegalArgumentException("Usuario ja possui vinculo com este evento");
        }

        UsuarioEvento relacao = UsuarioEvento.builder()
                .evento(evento)
                .usuario(usuario)
                .tipoRelacao(TipoRelacaoEvento.ORGANIZADOR.getCodigo())
                .build();

        return toResponse(repository.save(relacao));
    }

    @Transactional
    public UsuarioEventoResponseDTO participarEvento(Long eventoId, ParticiparEventoRequestDTO request) {
        TipoRelacaoEvento tipo = TipoRelacaoEvento.fromCodigo(request.getTipoRelacao());
        if (tipo == TipoRelacaoEvento.ORGANIZADOR) {
            throw new IllegalArgumentException("Organizador e definido apenas na criacao do evento");
        }

        Evento evento = buscarEvento(eventoId);
        Usuario usuario = buscarUsuario(request.getUsuarioId());

        if (repository.existsByEventoIdAndUsuarioId(eventoId, request.getUsuarioId())) {
            throw new IllegalArgumentException("Usuario ja possui vinculo com este evento");
        }

        if (tipo == TipoRelacaoEvento.COLABORADOR) {
            insumoEventoService.confirmarResponsabilidade(eventoId, usuario, request.getInsumoIds());
        }

        UsuarioEvento relacao = UsuarioEvento.builder()
                .evento(evento)
                .usuario(usuario)
                .tipoRelacao(tipo.getCodigo())
                .build();

        return toResponse(repository.save(relacao));
    }

    public List<UsuarioEventoResponseDTO> listarRelacoesPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento nao encontrado");
        }

        return repository.findByEventoId(eventoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<UsuarioEventoResponseDTO> listarRelacoesPorUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario nao encontrado");
        }

        return repository.findByUsuarioId(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void removerRelacao(Long eventoId, Long usuarioId) {
        UsuarioEvento relacao = repository.findByEventoIdAndUsuarioId(eventoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo do usuario com o evento nao encontrado"));

        if (TipoRelacaoEvento.fromCodigo(relacao.getTipoRelacao()) == TipoRelacaoEvento.ORGANIZADOR) {
            throw new IllegalArgumentException("Organizador nao pode ser removido por este endpoint");
        }

        repository.delete(relacao);
    }

    private Evento buscarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento nao encontrado"));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private UsuarioEventoResponseDTO toResponse(UsuarioEvento relacao) {
        TipoRelacaoEvento tipo = TipoRelacaoEvento.fromCodigo(relacao.getTipoRelacao());

        return UsuarioEventoResponseDTO.builder()
                .id(relacao.getId())
                .eventoId(relacao.getEvento().getId())
                .nomeEvento(relacao.getEvento().getNome())
                .usuarioId(relacao.getUsuario().getId())
                .nomeUsuario(relacao.getUsuario().getNome())
                .tipoRelacao(tipo.getCodigo())
                .descricaoTipoRelacao(tipo.name())
                .build();
    }
}
