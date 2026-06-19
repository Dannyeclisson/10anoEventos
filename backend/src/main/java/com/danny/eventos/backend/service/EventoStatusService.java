package com.danny.eventos.backend.service;

import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.model.StatusEvento;
import com.danny.eventos.backend.model.StatusInscricao;
import com.danny.eventos.backend.model.TipoRelacaoEvento;
import com.danny.eventos.backend.repository.EventoRepository;
import com.danny.eventos.backend.repository.UsuarioEventoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventoStatusService {

    private static final List<TipoRelacaoEvento> INSCRICOES_ATIVAS = List.of(
            TipoRelacaoEvento.PARTICIPANTE,
            TipoRelacaoEvento.COLABORADOR
    );

    private final EventoRepository eventoRepository;
    private final UsuarioEventoRepository usuarioEventoRepository;

    public EventoStatusService(
            EventoRepository eventoRepository,
            UsuarioEventoRepository usuarioEventoRepository
    ) {
        this.eventoRepository = eventoRepository;
        this.usuarioEventoRepository = usuarioEventoRepository;
    }

    @Transactional
    public Evento atualizarStatusCalculado(Evento evento) {
        LocalDateTime agora = LocalDateTime.now();
        StatusEvento statusEvento = calcularStatusEvento(evento, agora);
        StatusInscricao statusInscricao = calcularStatusInscricao(evento, statusEvento, agora);

        if (evento.getStatusEvento() == statusEvento && evento.getStatusInscricao() == statusInscricao) {
            return evento;
        }

        evento.setStatusEvento(statusEvento);
        evento.setStatusInscricao(statusInscricao);

        return eventoRepository.save(evento);
    }

    public StatusEvento calcularStatusEvento(Evento evento, LocalDateTime agora) {
        StatusEvento statusAtual = evento.getStatusEvento();
        LocalDateTime dataInicio = resolverDataInicio(evento);
        LocalDateTime dataFim = resolverDataFim(evento);

        if (statusAtual == StatusEvento.CANCELADO) {
            return StatusEvento.CANCELADO;
        }

        if (!agora.isBefore(dataFim)) {
            return StatusEvento.FINALIZADO;
        }

        if (!agora.isBefore(dataInicio) && agora.isBefore(dataFim)) {
            return StatusEvento.EM_ANDAMENTO;
        }

        if (statusAtual == StatusEvento.ADIADO) {
            return StatusEvento.ADIADO;
        }

        return StatusEvento.AGENDADO;
    }

    public StatusInscricao calcularStatusInscricao(Evento evento, StatusEvento statusEvento, LocalDateTime agora) {
        LocalDateTime dataInicioInscricoes = resolverDataInicioInscricoes(evento);
        LocalDateTime dataInicioEvento = resolverDataInicio(evento);

        if (statusEvento == StatusEvento.CANCELADO
                || statusEvento == StatusEvento.FINALIZADO
                || statusEvento == StatusEvento.EM_ANDAMENTO) {
            return StatusInscricao.ENCERRADA;
        }

        if (agora.isBefore(dataInicioInscricoes)) {
            return StatusInscricao.NAO_ABERTA;
        }

        if (!agora.isBefore(dataInicioEvento)) {
            return StatusInscricao.ENCERRADA;
        }

        if (contarInscritosAtivos(evento.getId()) >= resolverCapacidadeParticipantes(evento)) {
            return StatusInscricao.LOTADA;
        }

        return StatusInscricao.ABERTA;
    }

    public long contarInscritosAtivos(Long eventoId) {
        if (eventoId == null) {
            return 0;
        }

        return usuarioEventoRepository.countByEventoIdAndTipoRelacaoIn(eventoId, INSCRICOES_ATIVAS);
    }

    private LocalDateTime resolverDataInicio(Evento evento) {
        if (evento.getDataInicio() != null) {
            return evento.getDataInicio();
        }

        return evento.getDataHora();
    }

    private LocalDateTime resolverDataFim(Evento evento) {
        if (evento.getDataFim() != null) {
            return evento.getDataFim();
        }

        return resolverDataInicio(evento).plusHours(2);
    }

    private LocalDateTime resolverDataInicioInscricoes(Evento evento) {
        if (evento.getDataInicioInscricoes() != null) {
            return evento.getDataInicioInscricoes();
        }

        return LocalDateTime.now();
    }

    private int resolverCapacidadeParticipantes(Evento evento) {
        return evento.getCapacidadeParticipantes() != null ? evento.getCapacidadeParticipantes() : Integer.MAX_VALUE;
    }
}
