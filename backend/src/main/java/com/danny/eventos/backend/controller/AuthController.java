package com.danny.eventos.backend.controller;

import com.danny.eventos.backend.config.AuthCookieService;
import com.danny.eventos.backend.dto.LoginRequestDTO;
import com.danny.eventos.backend.dto.UsuarioResponseDTO;
import com.danny.eventos.backend.exception.ErrorResponse;
import com.danny.eventos.backend.service.AuthService;
import com.danny.eventos.backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Login, consulta da sessão atual e logout")
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
    @Operation(
            summary = "Realizar login",
            description = "Autentica com email e senha e envia o JWT em cookie HttpOnly."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Email ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "Consultar usuário autenticado", description = "Retorna os dados do usuário da sessão atual.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado retornado"),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UsuarioResponseDTO me(HttpServletRequest request) {
        return authService.me(extrairToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar sessão", description = "Expira o cookie HttpOnly usado na autenticação.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sessão encerrada"),
            @ApiResponse(responseCode = "401", description = "Sessão ausente ou inválida",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
