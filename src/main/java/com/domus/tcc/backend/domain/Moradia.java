package com.domus.tcc.backend.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "moradia")
@Getter 
@Setter
@NoArgsConstructor 
@AllArgsConstructor
public class Moradia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMoradia")
    private Long id;

    @Column(nullable = false, length = 10)
    private String numero; // Ex: "101", "Apto 22"

    // Muitas moradias pertencem a UM bloco
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloco_idBloco", nullable = false)
    private Bloco bloco;

    // Uma moradia pode ter vários moradores (família, inquilinos)
    // O 'mappedBy' aponta para o nome do atributo 'moradia' dentro da classe Morador
    @OneToMany(mappedBy = "moradia")
    private List<Morador> moradores = new ArrayList<>();
}