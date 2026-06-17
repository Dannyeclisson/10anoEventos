package com.danny.eventos.backend.service;

import com.danny.eventos.backend.dto.LoginRequestDTO;
import com.danny.eventos.backend.dto.UsuarioResponseDTO;
import com.danny.eventos.backend.exception.UnauthorizedException;
import com.danny.eventos.backend.model.Usuario;
import com.danny.eventos.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResult login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha invalidos"));

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            throw new UnauthorizedException("Email ou senha invalidos");
        }

        return new LoginResult(jwtService.gerarToken(usuario.getEmail()), toResponse(usuario));
    }

    public UsuarioResponseDTO me(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Cookie de autenticacao ausente");
        }

        String email = jwtService.extrairEmailValido(token);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Usuario do token nao encontrado"));

        return toResponse(usuario);
    }

    public UsuarioResponseDTO toResponse(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .dataNascimento(usuario.getDataNascimento())
                .cpf(usuario.getCpf())
                .telefone(usuario.getTelefone())
                .tipo(usuario.getTipo())
                .build();
    }

    public record LoginResult(String token, UsuarioResponseDTO usuario) {
    }
}
