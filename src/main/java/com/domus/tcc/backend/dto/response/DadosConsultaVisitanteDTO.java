package com.domus.tcc.backend.dto.response;

import java.util.List;

import com.domus.tcc.backend.domain.Visitante;

public record DadosConsultaVisitanteDTO(

        Long idVisitante,
        String nome,
        String dataHoraChegada,
        Long idTipoVisita,
        Long idPorteiro,
        List<Long> idsMoradias

) {

    public DadosConsultaVisitanteDTO(Visitante visitante) {
        this(
            visitante.getId(),
            visitante.getNome(),
            visitante.getDataHoraChegada().toString(),
            visitante.getIdTipoVisita(),
            visitante.getPorteiro().getId(),
            visitante.getMoradias()
                    .stream()
                    .map(moradia -> moradia.getId())
                    .toList()
        );
    }
}