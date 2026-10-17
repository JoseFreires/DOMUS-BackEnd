package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Moradia;
import com.domus.tcc.backend.domain.Visitante;

public record DadosConsultaVisitanteDTO(

        Long idVisitante,
        String nome,
        String dataHoraChegada,
        Long idTipoVisita,
        Long idPorteiro,
        Long idMoradia

) {

    public DadosConsultaVisitanteDTO(Visitante visitante, Moradia moradia) {
        this(
            visitante.getId(),
            visitante.getNome(),
            visitante.getDataHoraChegada().toString(),
            visitante.getIdTipoVisita(),
            visitante.getPorteiro().getId(),
            moradia.getId()
        );
    }

}