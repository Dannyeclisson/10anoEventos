package com.danny.eventos.backend.service;

import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.repository.EventoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventoService {

    private final EventoRepository repository;

    public EventoService(EventoRepository repository) {
        this.repository = repository;
    }

    public Evento salvar(Evento evento) {
        return repository.save(evento);
    }

    public List<Evento> listar() {
        return repository.findAll();
    }
}