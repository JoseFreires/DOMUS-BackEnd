package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Moradia;

public record DadosConsultaMoradiaResumoDTO (

        Long idMoradia,

        String numero
) {
    public DadosConsultaMoradiaResumoDTO(Moradia moradia) {
        this(
                moradia.getId(),
                moradia.getNumero()
        );
    }

}

