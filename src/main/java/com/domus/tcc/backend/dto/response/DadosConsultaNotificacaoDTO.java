package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Notificacao;

import java.time.LocalDateTime;

public record DadosConsultaNotificacaoDTO(
        Long idNotificacao,
        String descricao,
        LocalDateTime dataHoraChegada
) {

    public DadosConsultaNotificacaoDTO(Notificacao notificacao) {
        this(notificacao.getId(), notificacao.getDescricao(), notificacao.getDataHoraChegada());
    }
}
