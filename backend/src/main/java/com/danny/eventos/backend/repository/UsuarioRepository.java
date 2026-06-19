package com.danny.eventos.backend.repository;

import com.danny.eventos.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpf(String cpf);

    boolean existsByTelefone(String telefone);

    Optional<Usuario> findByEmail(String email);
}
