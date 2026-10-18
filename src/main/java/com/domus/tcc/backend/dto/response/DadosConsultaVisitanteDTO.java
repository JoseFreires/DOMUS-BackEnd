package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Moradia;
import com.domus.tcc.backend.domain.Visitante;

public record DadosConsultaVisitanteDTO(

        Long idVisitante,
        String nome,
        String dataHoraChegada,
        Long idTipoVisita,
        String nomeTipoVisita,
        Long idPorteiro,
        String nomePorteiro,
        Long idMoradia,
        String moradia,
        String bloco

) {

    public DadosConsultaVisitanteDTO(Visitante visitante, Moradia moradia) {
        this(
            visitante.getId(),
            visitante.getNome(),
            visitante.getDataHoraChegada().toString(),
            visitante.getTipoVisita().getId(),
            visitante.getTipoVisita().getNomeVisita(),
            visitante.getPorteiro().getId(),
            visitante.getPorteiro().getPessoa().getNomeCompleto(),
            moradia.getId(),
            moradia.getNumero(),
            moradia.getBloco().getNome_torre()
        );
    }

}