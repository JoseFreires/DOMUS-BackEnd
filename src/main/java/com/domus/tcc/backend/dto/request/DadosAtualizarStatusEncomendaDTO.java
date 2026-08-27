package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.domain.enums.TipoRetirada;
import jakarta.validation.constraints.NotNull;


public record DadosAtualizarStatusEncomendaDTO(

    @NotNull(message = "Quem retirou deve ser informado")
    TipoRetirada tipoRetirada

){
}