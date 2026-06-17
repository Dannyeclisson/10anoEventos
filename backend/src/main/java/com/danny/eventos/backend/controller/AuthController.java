package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.config.AuthCookieService;
import com.danny.eventos.backend.dto.LoginRequestDTO;
import com.danny.eventos.backend.dto.UsuarioResponseDTO;
import com.danny.eventos.backend.service.AuthService;
import com.danny.eventos.backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthCookieService authCookieService;

    public AuthController(
            AuthService authService,
            JwtService jwtService,
            AuthCookieService authCookieService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authCookieService = authCookieService;
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthService.LoginResult result = authService.login(request);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        authCookieService.criarCookieLogin(
                                result.token(),
                                jwtService.getExpirationMs()
                        ).toString()
                )
                .body(result.usuario());
    }

    @GetMapping("/me")
    public UsuarioResponseDTO me(HttpServletRequest request) {
        return authService.me(extrairToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieService.criarCookieLogout().toString())
                .build();
    }

    private String extrairToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> authCookieService.getCookieName().equals(cookie.getName()))
                .map(jakarta.servlet.http.Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
