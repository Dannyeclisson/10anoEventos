package com.danny.eventos.backend.repository;

import com.danny.eventos.backend.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {
}