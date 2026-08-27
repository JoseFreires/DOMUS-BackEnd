package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.dto.DadosLoginDTO;
import com.domus.tcc.backend.domain.enums.TurnoPorteiro;

import java.time.LocalDate;

public record DadosAtualizacaoPorteiroDTO(

        TurnoPorteiro turno,

        String empresaResponsavel,

        String nomeCompleto,

        String telefone,

        LocalDate dataNascimento,

        DadosRegistrarPessoaDTO pessoa,

        DadosLoginDTO usuario
) {
}
