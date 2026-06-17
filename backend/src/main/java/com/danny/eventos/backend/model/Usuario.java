package com.danny.eventos.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senhaHash;

    private LocalDate dataNascimento;

    @Column(unique = true, length = 14)
    private String cpf;

    private String telefone;

    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;
}
