package com.domus.tcc.backend.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.domus.tcc.backend.dto.request.DadosRegistrarVisitanteDTO;
import com.domus.tcc.backend.security.Usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "visitante")
public class Visitante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVisitante")
    private Long id;

    @Column(name = "nome", nullable = false, length = 50)
    private String nome;

    @Column(name = "data_hora_chegada", nullable = false)
    private LocalDateTime dataHoraChegada;

    @Column(name = "tipo_visita_idtipo_visita", nullable = false)
    private Long idTipoVisita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_porteiro", nullable = false)
    private Usuario porteiro;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "visitante_moradia",
        joinColumns = @JoinColumn(
            name = "visitante_idVisitante",
            referencedColumnName = "idVisitante"
        ),
        inverseJoinColumns = @JoinColumn(
            name = "moradia_idMoradia",
            referencedColumnName = "idMoradia"
        )
    )
    private List<Moradia> moradias = new ArrayList<>();

    public Visitante(
            DadosRegistrarVisitanteDTO dados,
            Usuario porteiro) {

        this.nome = dados.nome();
        this.dataHoraChegada = LocalDateTime.now();
        this.idTipoVisita = dados.idTipoVisita();
        this.porteiro = porteiro;
    }
}