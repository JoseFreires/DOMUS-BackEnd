package com.domus.tcc.backend.domain;

import java.time.LocalDate;

import com.domus.tcc.backend.dto.request.DadosRegistrarPessoaDTO;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Table(name = "pessoa")
@Entity(name = "Pessoa")
@EqualsAndHashCode(of = "id")
@Getter
@Setter
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idPessoa")
    private Long id;

    @Column(name = "nome_completo")
    private String nomeCompleto;

    @Column(unique = true)
    private String cpf;

    private Boolean ativo;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String telefone;

    @Column(name = "foto_perfil")
    private String fotoPerfil;

    private String email;

    @OneToOne(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Morador morador;

    @OneToOne(mappedBy = "pessoa", cascade = CascadeType.ALL, orphanRemoval = true)
    private Porteiro porteiro;

    public Pessoa(DadosRegistrarPessoaDTO dados, String fotoPerfil) {
        this.nomeCompleto = dados.nomeCompleto();
        this.cpf = dados.cpf();
        this.email = dados.email();
        this.telefone = dados.telefone();
        this.dataNascimento = dados.dataNascimento();
        this.ativo = true;
        this.fotoPerfil = fotoPerfil;
    }
}