package com.domus.tcc.backend.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.domus.tcc.backend.dto.request.DadosRegistrarMoradorDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
        this.moradia = moradia;
    }

    public Morador(){
        
    }

}
