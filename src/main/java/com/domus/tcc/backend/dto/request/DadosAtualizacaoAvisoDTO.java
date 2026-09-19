package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.domain.enums.TipoAviso;

import java.time.LocalDate;

public record DadosAtualizacaoAvisoDTO(
        String titulo,
        String descricao,
        Boolean prioridade,
        TipoAviso tipoAviso,
        LocalDate dataValidadeAviso) {
}
