package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.InsumoEventoRequestDTO;
import com.danny.eventos.backend.dto.InsumoEventoResponseDTO;
import com.danny.eventos.backend.exception.ResourceNotFoundException;
import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.model.InsumoEvento;
import com.danny.eventos.backend.model.StatusInsumo;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.repository.EventoRepository;
import com.danny.eventos.backend.repository.InsumoEventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InsumoEventoService {

    private final InsumoEventoRepository repository;
    private final EventoRepository eventoRepository;

    public InsumoEventoService(
            InsumoEventoRepository repository,
            EventoRepository eventoRepository
    ) {
        this.repository = repository;
        this.eventoRepository = eventoRepository;
    }

    @Transactional
    public List<InsumoEventoResponseDTO> salvarInsumos(Evento evento, List<InsumoEventoRequestDTO> insumos) {
        if (insumos == null || insumos.isEmpty()) {
            return List.of();
        }

        return insumos.stream()
                .map(request -> repository.save(toEntity(evento, request)))
                .map(this::toResponse)
                .toList();
    }

    public List<InsumoEventoResponseDTO> listarPorEvento(Long eventoId) {
        if (!eventoRepository.existsById(eventoId)) {
            throw new ResourceNotFoundException("Evento nao encontrado");
        }

        return repository.findByEventoId(eventoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InsumoEventoResponseDTO adicionarInsumo(Long eventoId, InsumoEventoRequestDTO request) {
        Evento evento = buscarEvento(eventoId);
        InsumoEvento insumo = repository.save(toEntity(evento, request));

        return toResponse(insumo);
    }

    @Transactional
    public InsumoEventoResponseDTO atualizarInsumo(Long eventoId, Long insumoId, InsumoEventoRequestDTO request) {
        InsumoEvento insumo = repository.findByIdAndEventoId(insumoId, eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo do evento nao encontrado"));

        insumo.setCategoria(request.getCategoria());
        insumo.setNome(request.getNome());
        insumo.setQuantidade(request.getQuantidade());
        insumo.setUnidadeMedida(request.getUnidadeMedida());
        insumo.setObservacoes(request.getObservacoes());
        insumo.setStatus(request.getStatus());

        return toResponse(repository.save(insumo));
    }

    @Transactional
    public void confirmarResponsabilidade(Long eventoId, Usuario responsavel, List<Long> insumoIds) {
        if (insumoIds == null || insumoIds.isEmpty()) {
            throw new IllegalArgumentException("Colaborador deve selecionar ao menos um insumo");
        }

        for (Long insumoId : insumoIds) {
            InsumoEvento insumo = repository.findByIdAndEventoId(insumoId, eventoId)
                    .orElseThrow(() -> new ResourceNotFoundException("Insumo do evento nao encontrado"));

            if (insumo.getResponsavel() != null
                    && !insumo.getResponsavel().getId().equals(responsavel.getId())) {
                throw new IllegalArgumentException("Insumo ja possui responsavel");
            }

            insumo.setResponsavel(responsavel);
            insumo.setStatus(StatusInsumo.CONFIRMADO);
            repository.save(insumo);
        }
    }

    @Transactional
    public void removerInsumo(Long eventoId, Long insumoId) {
        InsumoEvento insumo = repository.findByIdAndEventoId(insumoId, eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Insumo do evento nao encontrado"));

        repository.delete(insumo);
    }

    private Evento buscarEvento(Long eventoId) {
        return eventoRepository.findById(eventoId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento nao encontrado"));
    }

    private InsumoEvento toEntity(Evento evento, InsumoEventoRequestDTO request) {
        return InsumoEvento.builder()
                .evento(evento)
                .categoria(request.getCategoria())
                .nome(request.getNome())
                .quantidade(request.getQuantidade())
                .unidadeMedida(request.getUnidadeMedida())
                .observacoes(request.getObservacoes())
                .status(request.getStatus())
                .build();
    }

    public InsumoEventoResponseDTO toResponse(InsumoEvento insumo) {
        return InsumoEventoResponseDTO.builder()
                .id(insumo.getId())
                .eventoId(insumo.getEvento().getId())
                .categoria(insumo.getCategoria())
                .nome(insumo.getNome())
                .quantidade(insumo.getQuantidade())
                .unidadeMedida(insumo.getUnidadeMedida())
                .observacoes(insumo.getObservacoes())
                .status(insumo.getStatus())
                .responsavelId(insumo.getResponsavel() != null ? insumo.getResponsavel().getId() : null)
                .nomeResponsavel(insumo.getResponsavel() != null ? insumo.getResponsavel().getNome() : null)
                .build();
    }
}
