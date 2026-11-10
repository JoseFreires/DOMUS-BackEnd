package com.domus.tcc.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_visita")
@Getter
@Setter
@NoArgsConstructor
public class TipoVisita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idtTipo_visita")
    private Long id;

    @Column(name = "nome_visita", nullable = false, length = 45)
    private String nomeVisita;
}
