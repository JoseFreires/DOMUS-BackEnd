package com.domus.tcc.backend.dto.response;

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
}
