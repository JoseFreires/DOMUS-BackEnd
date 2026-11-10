package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.infra.aop.LogSistema;
import com.domus.tcc.backend.security.Usuario;

import java.time.LocalDateTime;

public record DadosConsultaLogDTO(

        Long idLog,

        String acaoRealizada,

        String tabelaAlterada,

        LocalDateTime dataHora,

        Long usuarioIdUsuario,

        Long contaAdmIdConta,

        String username,

        String nomeContaAdm
) {

    public DadosConsultaLogDTO(LogSistema logSistema) {
        this(
                logSistema.getId(),
                logSistema.getAcaoRealizada(),
                logSistema.getTabelaAlterada(),
                logSistema.getDataHora(),
                logSistema.getUsuario() != null ? logSistema.getUsuario().getId() : null,
                logSistema.getContaAdm() != null ? logSistema.getContaAdm().getIdContaAdm() : null,
                logSistema.getUsuario() != null ? logSistema.getUsuario().getUsername() : null,
                logSistema.getContaAdm() != null ? logSistema.getContaAdm().getNomeConta() : null);
    }
}
