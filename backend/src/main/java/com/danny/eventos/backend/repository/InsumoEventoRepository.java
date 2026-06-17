package com.danny.eventos.backend.repository;

import com.danny.eventos.backend.model.InsumoEvento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InsumoEventoRepository extends JpaRepository<InsumoEvento, Long> {

    List<InsumoEvento> findByEventoId(Long eventoId);

    Optional<InsumoEvento> findByIdAndEventoId(Long id, Long eventoId);

    void deleteByIdAndEventoId(Long id, Long eventoId);
}
