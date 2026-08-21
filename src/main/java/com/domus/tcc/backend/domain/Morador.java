package com.domus.tcc.backend.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.domus.tcc.backend.dto.request.DadosRegistrarMoradorDTO;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "morador")
public class Morador {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMorador")
    private Long id;

    @Column(name = "data_chegada", nullable = false)
    private LocalDate dataChegada;

    @Column(name = "data_saida")
    private LocalDateTime dataSaida;

    // Conforme discutimos, a foto é uma URL (String)
    @Column(name = "foto_perfil")
    private String urlFoto;

    // Relacionamento com a Moradia (Apartamento/Casa)
    // Muitos moradores podem pertencer a uma moradia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moradia_idMoradia", nullable = false)
    private Moradia moradia;

    @OneToOne
    @JoinColumn(name = "Pessoa_idPessoa", nullable = false, unique = true)
    private Pessoa pessoa;


    @OneToMany(mappedBy = "morador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PessoaAutorizada> pessoasAutorizadas = new ArrayList<>();


    public Morador(DadosRegistrarMoradorDTO dados, Moradia moradia) {
        this.dataChegada = dados.dataChegada();
        this.urlFoto = dados.fotoPerfil();
        this.moradia = moradia;
    }

    public Morador(){
        
    }

}
