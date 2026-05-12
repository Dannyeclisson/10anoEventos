package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.dto.EventoRequest;
import com.danny.eventos.backend.dto.EventoResponse;
import com.danny.eventos.backend.service.EventoService;
import jakarta.validation.Valid;
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
    public EventoResponse criar(@Valid @RequestBody EventoRequest request) {
        return service.salvar(request);
    }

    @GetMapping
    public List<EventoResponse> listar() {
        return service.listar();
    }
}