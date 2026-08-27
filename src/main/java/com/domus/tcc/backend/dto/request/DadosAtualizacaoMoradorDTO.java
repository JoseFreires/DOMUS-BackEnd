package com.domus.tcc.backend.dto.request;

import java.time.LocalDate;

public record DadosAtualizacaoMoradorDTO(

        LocalDate dataChegada,

        LocalDate dataSaida,

        String fotoPerfil,


        Long moradiaIdMoradia,

        String nomeCompleto,

        String telefone,

        LocalDate dataNascimento
) {
}
