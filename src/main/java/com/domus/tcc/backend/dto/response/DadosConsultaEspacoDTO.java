package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.EspacoCondominial;
import jakarta.persistence.*;

public record DadosConsultaEspacoDTO(

        Long idEspacoCondominial,
        String nomeEspaco,
        String descricao,
        String capacidadeMaxima,
        Double valorReserva,
        String fotoEspaco,
        Boolean ativo,
        String restricao,
        Long idCondominio,
        Long idSindico

) {
    public DadosConsultaEspacoDTO(EspacoCondominial espacoCondominial){

        this(
        espacoCondominial.getIdEspacoCondominial(),
        espacoCondominial.getNomeEspaco(),
        espacoCondominial.getDescricao(),
        espacoCondominial.getCapacidadeMaxima(),
        espacoCondominial.getValorReserva(),
        espacoCondominial.getFotoEspaco(),
        espacoCondominial.getAtivo(),
        espacoCondominial.getRestricao(),
        espacoCondominial.getCondominio().getId(),
        espacoCondominial.getSindico().getId()
        );

    }
}
