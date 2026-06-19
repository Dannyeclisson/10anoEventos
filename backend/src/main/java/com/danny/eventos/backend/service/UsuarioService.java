package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.UsuarioCadastroDTO;
import com.danny.eventos.backend.dto.UsuarioResponseDTO;
import com.danny.eventos.backend.exception.ResourceNotFoundException;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.repository.UsuarioRepository;
import com.danny.eventos.backend.validation.CpfValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    private UsuarioResponseDTO toResponse(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .dataNascimento(usuario.getDataNascimento())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .build();
    }

    public UsuarioResponseDTO cadastrar(UsuarioCadastroDTO request) {
        String email = request.getEmail().trim().toLowerCase();
        String cpf = CpfValidator.somenteDigitos(request.getCpf());
        String telefone = somenteDigitos(request.getTelefone());

        if (telefone.isBlank()) {
            throw new IllegalArgumentException("Telefone é obrigatório");
        }

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        if (repository.existsByCpf(cpf)) {
            throw new IllegalArgumentException("CPF já cadastrado");
        }

        if (repository.existsByTelefone(telefone)) {
            throw new IllegalArgumentException("Telefone já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(email)
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .dataNascimento(request.getDataNascimento())
                .cpf(cpf)
                .telefone(telefone)
                .build();

        Usuario salvo = repository.save(usuario);

        return toResponse(salvo);
    }

    public List<UsuarioResponseDTO> listar() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        return toResponse(usuario);
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }
}
