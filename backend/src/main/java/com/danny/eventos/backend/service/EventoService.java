package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.CancelarEventoRequestDTO;
import com.danny.eventos.backend.dto.EventoRequest;
import com.danny.eventos.backend.dto.EventoResponse;
import com.danny.eventos.backend.exception.ResourceNotFoundException;
import com.danny.eventos.backend.exception.ForbiddenOperationException;
import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.model.StatusEvento;
import com.danny.eventos.backend.model.StatusInscricao;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.repository.EventoRepository;
import com.danny.eventos.backend.repository.InsumoEventoRepository;
import com.danny.eventos.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.List;

@Service
public class EventoService {

    private final EventoRepository repository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioEventoService usuarioEventoService;
    private final InsumoEventoRepository insumoEventoRepository;
    private final InsumoEventoService insumoEventoService;
    private final EventoStatusService eventoStatusService;

    public EventoService(
            EventoRepository repository,
            UsuarioRepository usuarioRepository,
            UsuarioEventoService usuarioEventoService,
            InsumoEventoRepository insumoEventoRepository,
            InsumoEventoService insumoEventoService,
            EventoStatusService eventoStatusService
    ) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioEventoService = usuarioEventoService;
        this.insumoEventoRepository = insumoEventoRepository;
        this.insumoEventoService = insumoEventoService;
        this.eventoStatusService = eventoStatusService;
    }

    private EventoResponse toResponse(Evento evento) {
        Evento eventoAtualizado = eventoStatusService.atualizarStatusCalculado(evento);

        return EventoResponse.builder()
                .id(eventoAtualizado.getId())
                .nome(eventoAtualizado.getNome())
                .descricao(eventoAtualizado.getDescricao())
                .local(eventoAtualizado.getLocal())
                .imagemUrl(eventoAtualizado.getImagemUrl())
                .dataHora(formatar(eventoAtualizado.getDataHora()))
                .dataInicio(formatar(eventoAtualizado.getDataInicio()))
                .dataFim(formatar(eventoAtualizado.getDataFim()))
                .dataInicioInscricoes(formatar(eventoAtualizado.getDataInicioInscricoes()))
                .capacidadeParticipantes(eventoAtualizado.getCapacidadeParticipantes())
                .statusEvento(eventoAtualizado.getStatusEvento().getValor())
                .statusInscricao(eventoAtualizado.getStatusInscricao().getValor())
                .dataCancelamento(formatar(eventoAtualizado.getDataCancelamento()))
                .motivoCancelamento(eventoAtualizado.getMotivoCancelamento())
                .organizadorId(eventoAtualizado.getOrganizador().getId())
                .organizadorNome(eventoAtualizado.getOrganizador().getNome())
                .participantes(eventoStatusService.contarInscritosAtivos(eventoAtualizado.getId()))
                .insumos(insumoEventoRepository.findByEventoId(eventoAtualizado.getId())
                        .stream()
                        .map(insumoEventoService::toResponse)
                        .toList())
                .build();
    }

    @Transactional
    public EventoResponse salvar(EventoRequest request, Long usuarioAutenticadoId) {
        DadosEvento dados = validarDadosEvento(request, true);
        Usuario organizador = usuarioRepository.findById(usuarioAutenticadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        Evento evento = Evento.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .local(request.getLocal())
                .imagemUrl(request.getImagemUrl())
                .dataHora(dados.dataInicio())
                .dataInicio(dados.dataInicio())
                .dataFim(dados.dataFim())
                .dataInicioInscricoes(dados.dataInicioInscricoes())
                .capacidadeParticipantes(dados.capacidadeParticipantes())
                .statusEvento(StatusEvento.AGENDADO)
                .statusInscricao(StatusInscricao.NAO_ABERTA)
                .organizador(organizador)
                .build();

        Evento salvo = repository.save(evento);
        usuarioEventoService.registrarOrganizador(salvo, organizador);
        insumoEventoService.salvarInsumos(salvo, request.getInsumos());
        eventoStatusService.atualizarStatusCalculado(salvo);

        return toResponse(salvo);
    }

    @Transactional
    public List<EventoResponse> listar() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EventoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Evento nao encontrado"));
    }

    @Transactional
    public EventoResponse buscarParaEdicao(Long id, Long usuarioAutenticadoId) {
        Evento evento = buscarEvento(id);
        validarOrganizador(evento, usuarioAutenticadoId);
        eventoStatusService.atualizarStatusCalculado(evento);
        bloquearEdicaoQuandoNecessario(evento);
        return toResponse(evento);
    }

    @Transactional
    public EventoResponse atualizar(Long id, EventoRequest request, Long usuarioAutenticadoId) {
        Evento evento = buscarEvento(id);
        validarOrganizador(evento, usuarioAutenticadoId);
        eventoStatusService.atualizarStatusCalculado(evento);
        bloquearEdicaoQuandoNecessario(evento);
        DadosEvento dados = validarDadosEvento(request, false);

        boolean alterouDataOuHorario = !Objects.equals(evento.getDataInicio(), dados.dataInicio())
                || !Objects.equals(evento.getDataFim(), dados.dataFim());

        evento.setNome(request.getNome());
        evento.setDescricao(request.getDescricao());
        evento.setLocal(request.getLocal());
        evento.setImagemUrl(request.getImagemUrl());
        evento.setDataHora(dados.dataInicio());
        evento.setDataInicio(dados.dataInicio());
        evento.setDataFim(dados.dataFim());
        evento.setDataInicioInscricoes(dados.dataInicioInscricoes());
        evento.setCapacidadeParticipantes(dados.capacidadeParticipantes());

        if (alterouDataOuHorario) {
            evento.setStatusEvento(StatusEvento.ADIADO);
        }

        return toResponse(repository.save(evento));
    }

    @Transactional
    public EventoResponse cancelar(Long id, CancelarEventoRequestDTO request, Long usuarioAutenticadoId) {
        Evento evento = buscarEvento(id);

        validarOrganizador(evento, usuarioAutenticadoId);
        eventoStatusService.atualizarStatusCalculado(evento);

        if (evento.getStatusEvento() == StatusEvento.CANCELADO) {
            throw new IllegalArgumentException("Evento ja esta cancelado");
        }

        if (evento.getStatusEvento() == StatusEvento.FINALIZADO) {
            throw new IllegalArgumentException("Evento finalizado nao pode ser cancelado");
        }

        evento.setStatusEvento(StatusEvento.CANCELADO);
        evento.setStatusInscricao(StatusInscricao.ENCERRADA);
        evento.setDataCancelamento(LocalDateTime.now());
        evento.setMotivoCancelamento(request.getMotivo());

        return toResponse(repository.save(evento));
    }

    private Evento buscarEvento(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento nao encontrado"));
    }

    private void validarOrganizador(Evento evento, Long organizadorId) {
        if (organizadorId == null || !evento.getOrganizador().getId().equals(organizadorId)) {
            throw new ForbiddenOperationException("Apenas o organizador pode realizar esta operação");
        }
    }

    private void bloquearEdicaoQuandoNecessario(Evento evento) {
        if (evento.getStatusEvento() == StatusEvento.CANCELADO) {
            throw new IllegalArgumentException("Evento cancelado nao pode ser editado");
        }

        if (evento.getStatusEvento() == StatusEvento.FINALIZADO) {
            throw new IllegalArgumentException("Evento finalizado nao pode ser editado");
        }
    }

    private DadosEvento validarDadosEvento(EventoRequest request, boolean criacao) {
        LocalDateTime dataInicio = parseData(request.getDataInicio(), "Data de inicio invalida");
        LocalDateTime dataFim = parseData(request.getDataFim(), "Data de fim invalida");
        LocalDateTime dataInicioInscricoes = parseData(
                request.getDataInicioInscricoes(),
                "Data de inicio das inscricoes invalida"
        );
        Integer capacidadeParticipantes = request.getCapacidadeParticipantes();
        LocalDateTime agora = LocalDateTime.now();

        if (!dataInicio.isAfter(agora)) {
            throw new IllegalArgumentException("Data de inicio do evento deve ser posterior ao momento atual");
        }

        if (!dataFim.isAfter(dataInicio)) {
            throw new IllegalArgumentException("Data de fim do evento deve ser posterior a data de inicio");
        }

        if (criacao && dataInicioInscricoes.isBefore(agora.minusSeconds(5))) {
            throw new IllegalArgumentException("Data de inicio das inscricoes deve ser igual ou posterior ao momento atual");
        }

        if (!dataInicioInscricoes.isBefore(dataInicio)) {
            throw new IllegalArgumentException("Data de inicio das inscricoes deve ser anterior ao inicio do evento");
        }

        if (capacidadeParticipantes == null || capacidadeParticipantes <= 1) {
            throw new IllegalArgumentException("Capacidade de participantes deve ser maior que 1");
        }

        return new DadosEvento(dataInicio, dataFim, dataInicioInscricoes, capacidadeParticipantes);
    }

    private LocalDateTime parseData(String valor, String mensagemErro) {
        try {
            return LocalDateTime.parse(valor);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException(mensagemErro);
        }
    }

    private String formatar(LocalDateTime data) {
        return data != null ? data.toString() : null;
    }

    private record DadosEvento(
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            LocalDateTime dataInicioInscricoes,
            Integer capacidadeParticipantes
    ) {
    }
}
