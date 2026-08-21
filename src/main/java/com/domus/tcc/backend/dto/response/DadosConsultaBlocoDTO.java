package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Bloco;

public record DadosConsultaBlocoDTO(

        Long idBloco,

        String nomeTorre,

        Long condominioIdCondominio,

        String nomeCondominio
) {
    public DadosConsultaBlocoDTO(Bloco bloco) {
        this(
                bloco.getId(),
                bloco.getNome_torre(),
                bloco.getCondominio().getId(),
                bloco.getCondominio().getNome_condominio()
        );
    }
}
