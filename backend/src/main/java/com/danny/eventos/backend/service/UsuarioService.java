package com.danny.eventos.backend.service;

import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario salvar(Usuario usuario) {
        return repository.save(usuario);
    }

    public List<Usuario> listar() {
        return repository.findAll();
    }
}