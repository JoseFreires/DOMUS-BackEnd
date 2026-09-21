package com.domus.tcc.backend.domain;

import com.domus.tcc.backend.domain.enums.TipoAviso;
import com.domus.tcc.backend.dto.request.DadosRegistrarAvisoDTO;
import com.domus.tcc.backend.security.Usuario;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.flywaydb.core.internal.util.BooleanEvaluator;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "AvisoCondominial")
@Table(name = "aviso_condominial")

public class AvisoCondominial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idaviso_condominial")
    private Long id;

    @Column(name = "titulo", length = 100)
    private String titulo;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "imagem")
    private String fotoAviso;

    @Column(name = "data_aviso")
    private LocalDateTime dataHoraAviso;

    @Column(name = "prioridade")
    private Boolean prioridade;

    @Column(name = "tipo_aviso")
    @Enumerated(EnumType.STRING)
    private TipoAviso tipoAviso;

    @Column(name = "data_validade")
    private LocalDate dataValidadeAviso;

    @Column(name = "ativo")
    private Boolean ativo;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "Condominio_idCondominio", nullable = false)
    private Condominio condominio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_sindico")
    private Usuario sindico;

    public AvisoCondominial(DadosRegistrarAvisoDTO dados, String fotoAviso, Usuario sindico, Condominio condominio){

        this.titulo = dados.titulo();
        this.descricao = dados.descricao();
        this.fotoAviso = fotoAviso;
        this.dataHoraAviso = LocalDateTime.now();
        this.prioridade = dados.prioridade();
        this.tipoAviso = dados.tipoAviso();
        this.dataValidadeAviso = dados.dataValidadeAviso();
        this.ativo = true;
        this.condominio = condominio;
        this.sindico = sindico;

    }

}
