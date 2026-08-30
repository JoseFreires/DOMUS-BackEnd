package com.domus.tcc.backend.dto.request;

import java.time.LocalDate;

public record DadosAtualizacaoMoradorDTO(

        LocalDate dataChegada,

        LocalDate dataSaida,

        Long moradiaIdMoradia,

        String nomeCompleto,

        String telefone,

        LocalDate dataNascimento
) {
}
