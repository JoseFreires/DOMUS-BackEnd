package com.domus.tcc.backend.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "bloco")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Bloco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBloco")
    private Long id;

    @Column(nullable = false, length = 45)
    private String nome_torre; // Ex: "Bloco A", "Torre 1"

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "Condominio_idCondominio", nullable = false)
    private Condominio condominio;

    @OneToMany(mappedBy = "bloco", cascade = CascadeType.ALL)
    private List<Moradia> moradias = new ArrayList<>();
}