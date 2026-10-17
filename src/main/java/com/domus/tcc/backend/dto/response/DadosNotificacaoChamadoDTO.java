package com.domus.tcc.backend.dto.response;

public record DadosNotificacaoChamadoDTO(
        String tipo,
        String mensagem,
        DadosConsultaChamadoDTO chamado
) {
}
