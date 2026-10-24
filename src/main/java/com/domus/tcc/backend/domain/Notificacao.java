package com.domus.tcc.backend.domain;

import com.domus.tcc.backend.security.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idNotificacao")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_destinatario", nullable = false)
    private Usuario usuarioDestinatario;

    @Column(name = "descricao", nullable = false, length = 255)
    private String descricao;

    @Column(name = "data_hora_chegada", nullable = false)
    private LocalDateTime dataHoraChegada;

    public Notificacao(Usuario usuarioDestinatario, String descricao) {
        this.usuarioDestinatario = usuarioDestinatario;
        this.descricao = descricao;
        this.dataHoraChegada = LocalDateTime.now();
    }
}
