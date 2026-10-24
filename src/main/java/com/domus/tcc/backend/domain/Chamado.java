package com.domus.tcc.backend.domain;

import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.domain.enums.TipoChamado;
import com.domus.tcc.backend.dto.request.DadosRegistrarChamadoDTO;
import com.domus.tcc.backend.security.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chamado")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idchamado")
    private Long id;

    @Column(name = "titulo", nullable = false, length = 45)
    private String titulo;

    @Column(name = "descricao", nullable = false, length = 300)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_chamado", nullable = false)
    private TipoChamado tipoChamado;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_chamado", nullable = false)
    private StatusChamado statusChamado;

    @Column(name = "imagem")
    private String imagem;

    @Column(name = "data_hora_abertura", nullable = false)
    private LocalDateTime dataHoraAbertura;

    @Column(name = "data_hora_finalizado")
    private LocalDateTime dataHoraFinalizado;

    @Column(name = "resolucao_chamado", length = 1000)
    private String resolucaoChamado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "morador_idMorador", nullable = false)
    private Morador morador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_sindico", nullable = false)
    private Usuario sindico;

    public Chamado(DadosRegistrarChamadoDTO dados, String imagem, Usuario sindico, Morador morador) {
        this.titulo = dados.titulo();
        this.descricao = dados.descricao();
        this.tipoChamado = dados.tipoChamado();
        this.statusChamado = StatusChamado.ABERTO;
        this.imagem = imagem;
        this.dataHoraAbertura = LocalDateTime.now();
        this.morador = morador;
        this.sindico = sindico;
    }
}
