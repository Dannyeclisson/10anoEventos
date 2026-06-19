package com.danny.eventos.backend.repository;

import com.danny.eventos.backend.model.InsumoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InsumoEventoRepository extends JpaRepository<InsumoEvento, Long> {

    List<InsumoEvento> findByEventoId(Long eventoId);

    Optional<InsumoEvento> findByIdAndEventoId(Long id, Long eventoId);

    void deleteByIdAndEventoId(Long id, Long eventoId);

    @Modifying
    @Query("""
            update InsumoEvento insumo
               set insumo.responsavel = null
             where insumo.evento.id = :eventoId
               and insumo.responsavel.id = :usuarioId
            """)
    int removerResponsavelPorEventoEUsuario(@Param("eventoId") Long eventoId, @Param("usuarioId") Long usuarioId);
}
