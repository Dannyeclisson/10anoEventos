package com.danny.eventos.backend.repository;

import com.danny.eventos.backend.model.UsuarioEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioEventoRepository extends JpaRepository<UsuarioEvento, Long> {

    boolean existsByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    Optional<UsuarioEvento> findByEventoIdAndUsuarioId(Long eventoId, Long usuarioId);

    List<UsuarioEvento> findByUsuarioId(Long usuarioId);

    List<UsuarioEvento> findByEventoId(Long eventoId);

    long countByEventoId(Long eventoId);
}
