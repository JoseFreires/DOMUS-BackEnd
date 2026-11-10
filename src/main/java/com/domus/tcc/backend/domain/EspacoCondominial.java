package com.domus.tcc.backend.domain;


import com.domus.tcc.backend.dto.request.DadosRegistrarEspacoDTO;
import com.domus.tcc.backend.security.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "EspacoCondominial")
@Table(name = "espaco_condominial")
public class EspacoCondominial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEspaco_condominial")
    private Long idEspacoCondominial;

    @Column(name = "nome")
    private String nomeEspaco;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "capacidade_maxima")
    private String capacidadeMaxima;

    @Column(name = "valor_reserva")
    private Double valorReserva;

    @Column(name = "imagem")
    private String fotoEspaco;

    @Column(name = "ativo")
    private Boolean ativo;

    @Column(name = "restricao")
    private String restricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominio_idCondominio", nullable = false)
    private Condominio condominio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_sindico")
    private Usuario sindico;

    public EspacoCondominial(DadosRegistrarEspacoDTO dados, String fotoEspaco, Usuario sindico, Condominio condominio){
        this.nomeEspaco = dados.nomeEspaco();
        this.descricao = dados.descricao();
        this.capacidadeMaxima = dados.capacidadeMaxima();
        this.restricao = dados.restricao();
        this.valorReserva = dados.valorReserva();
        this.sindico = sindico;
        this.condominio = condominio;
        this.fotoEspaco = fotoEspaco;
        this.ativo = true;
    }

}
