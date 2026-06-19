package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.CancelarInscricaoRequestDTO;
import com.danny.eventos.backend.dto.ParticiparEventoRequestDTO;
import com.danny.eventos.backend.dto.UsuarioEventoResponseDTO;
import com.danny.eventos.backend.exception.ResourceNotFoundException;
import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.model.StatusInscricao;
import com.danny.eventos.backend.model.TipoRelacaoEvento;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.model.UsuarioEvento;
import com.danny.eventos.backend.repository.EventoRepository;
import com.danny.eventos.backend.repository.InsumoEventoRepository;
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
    private final InsumoEventoRepository insumoEventoRepository;
    private final EventoStatusService eventoStatusService;

    public UsuarioEventoService(
            UsuarioEventoRepository repository,
            EventoRepository eventoRepository,
            UsuarioRepository usuarioRepository,
            InsumoEventoService insumoEventoService,
            InsumoEventoRepository insumoEventoRepository,
            EventoStatusService eventoStatusService
    ) {
        this.repository = repository;
        this.eventoRepository = eventoRepository;
        this.usuarioRepository = usuarioRepository;
        this.insumoEventoService = insumoEventoService;
        this.insumoEventoRepository = insumoEventoRepository;
        this.eventoStatusService = eventoStatusService;
    }

    @Transactional
    public UsuarioEventoResponseDTO registrarOrganizador(Evento evento, Usuario usuario) {
        if (repository.existsByEventoIdAndUsuarioId(evento.getId(), usuario.getId())) {
            throw new IllegalArgumentException("Usuario ja possui vinculo com este evento");
        }

        UsuarioEvento relacao = UsuarioEvento.builder()
                .evento(evento)
                .usuario(usuario)
                .tipoRelacao(TipoRelacaoEvento.ORGANIZADOR)
                .build();

        return toResponse(repository.save(relacao));
    }

    @Transactional
    public UsuarioEventoResponseDTO participarEvento(Long eventoId, ParticiparEventoRequestDTO request) {
        TipoRelacaoEvento tipo = TipoRelacaoEvento.fromCodigo(request.getTipoRelacao());
        if (tipo == TipoRelacaoEvento.ORGANIZADOR || tipo == TipoRelacaoEvento.CANCELADO) {
            throw new IllegalArgumentException("Tipo de relacao nao permitido para inscricao");
        }

        Evento evento = buscarEvento(eventoId);
        eventoStatusService.atualizarStatusCalculado(evento);

        if (evento.getStatusInscricao() != StatusInscricao.ABERTA) {
            throw new IllegalArgumentException("Inscricoes nao estao abertas para este evento");
        }

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
                .tipoRelacao(tipo)
                .build();

        UsuarioEvento salvo = repository.save(relacao);
        eventoStatusService.atualizarStatusCalculado(evento);

        return toResponse(salvo);
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

        if (relacao.getTipoRelacao() == TipoRelacaoEvento.ORGANIZADOR) {
            throw new IllegalArgumentException("Organizador nao pode ser removido por este endpoint");
        }

        cancelarRelacao(relacao);
        eventoStatusService.atualizarStatusCalculado(relacao.getEvento());
    }

    @Transactional
    public UsuarioEventoResponseDTO cancelarInscricao(Long eventoId, CancelarInscricaoRequestDTO request) {
        UsuarioEvento relacao = repository.findByEventoIdAndUsuarioId(eventoId, request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo do usuario com o evento nao encontrado"));

        if (relacao.getTipoRelacao() == TipoRelacaoEvento.ORGANIZADOR) {
            throw new IllegalArgumentException("Organizador nao pode cancelar inscricao por este endpoint");
        }

        if (relacao.getTipoRelacao() == TipoRelacaoEvento.CANCELADO) {
            throw new IllegalArgumentException("Inscricao ja esta cancelada");
        }

        UsuarioEvento cancelada = cancelarRelacao(relacao);
        eventoStatusService.atualizarStatusCalculado(cancelada.getEvento());

        return toResponse(cancelada);
    }

    private UsuarioEvento cancelarRelacao(UsuarioEvento relacao) {
        if (relacao.getTipoRelacao() == TipoRelacaoEvento.COLABORADOR) {
            insumoEventoRepository.removerResponsavelPorEventoEUsuario(
                    relacao.getEvento().getId(),
                    relacao.getUsuario().getId()
            );
        }

        relacao.setTipoRelacao(TipoRelacaoEvento.CANCELADO);
        return repository.save(relacao);
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
        TipoRelacaoEvento tipo = relacao.getTipoRelacao();

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
