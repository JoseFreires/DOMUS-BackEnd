package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Bloco;

import java.util.List;

public record DadosConsultaBlocoMoradiasDTO(
        Long idBloco,

        String nomeTorre,

        Long condominioIdCondominio,

        String nomeCondominio,

        List<DadosConsultaMoradiaResumoDTO> moradias
) {
    public DadosConsultaBlocoMoradiasDTO(Bloco bloco) {
        this(
                bloco.getId(),
                bloco.getNome_torre(),
                bloco.getCondominio().getId(),
                bloco.getCondominio().getNome_condominio(),
                bloco.getMoradias()
                        .stream()
                        .map(DadosConsultaMoradiaResumoDTO::new)
                        .toList()
        );
    }
}
