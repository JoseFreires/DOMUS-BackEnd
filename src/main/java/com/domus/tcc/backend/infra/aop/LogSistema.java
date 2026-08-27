package com.domus.tcc.backend.infra.aop;

import java.time.LocalDateTime;

import com.domus.tcc.backend.security.Usuario;

import com.domus.tcc.backend.domain.ContaAdm;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "log_sistema")
@Getter
@Setter
@NoArgsConstructor
public class LogSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLog")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "Usuario_idUsuario", nullable = true)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "conta_adm_idConta", nullable = true)
    private ContaAdm contaAdm;

    @Column(name = "acao_realizada")
    private String acaoRealizada;

    @Column(name = "tabela_alterada")
    private String tabelaAlterada;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;
}