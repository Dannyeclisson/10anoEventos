package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.model.Evento;
import com.danny.eventos.backend.service.EventoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final EventoService service;

    public EventoController(EventoService service) {
        this.service = service;
    }

    @PostMapping
    public Evento criar(@RequestBody Evento evento) {
        return service.salvar(evento);
    }

    @GetMapping
    public List<Evento> listar() {
        return service.listar();
    }
}