package com.domus.tcc.backend.domain;

import java.time.LocalDateTime;

import com.domus.tcc.backend.dto.request.DadosRegistrarEncomendaDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.domain.enums.StatusEncomenda;
import com.domus.tcc.backend.domain.enums.TipoRetirada;

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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity(name = "Encomenda")
@Table(name = "encomenda")
public class Encomenda {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEncomenda")
    private Long idEncomenda;

    @Column(name = "nome_pacote")
    private String nomePacote;

    @Column(name = "observacao")
    private String observacao;

    @Column(name = "data_hora_recebido")
    private LocalDateTime dataHoraRecebido;

    @Column(name = "data_hora_retirado")
    private LocalDateTime dataHoraRetirado;

    @Column(name = "foto_encomenda")
    private String fotoEncomenda;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusEncomenda statusEncomenda;

    @Column(name = "token")
    private String token;

    @Column(name = "tipo_retirada")
    @Enumerated(EnumType.STRING)
    private TipoRetirada tipoRetirada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_porteiro")
    private Usuario porteiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pessoa_destinatario", nullable = false)
    private Pessoa moradorDestinatario;

    // Construtor atualizado
    public Encomenda(DadosRegistrarEncomendaDTO dados, Usuario porteiro, Pessoa moradorDestinatario, String token, String fotoEncomenda) {
        this.nomePacote = dados.nomePacote();
        this.dataHoraRecebido = LocalDateTime.now();
        this.observacao = dados.observacao();
        this.statusEncomenda = StatusEncomenda.RECEBIDA;
        this.porteiro = porteiro;
        this.moradorDestinatario = moradorDestinatario;
        this.fotoEncomenda = fotoEncomenda; 
        this.token = token;
    }

}
    






