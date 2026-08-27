package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.PessoaAutorizada;

public record DadosConsultaPessoaAutorizadaDTO(

        Long idPessoaAutorizada,

        String nome,

        String cpf,

        Long moradorIdMorador,

        String nomeMorador,

        String numeroMoradia
) {
    public DadosConsultaPessoaAutorizadaDTO(PessoaAutorizada pessoaAutorizada){
        this(

                pessoaAutorizada.getIdPessoaAutorizada(),
                pessoaAutorizada.getNome(),
                pessoaAutorizada.getCpf(),
                pessoaAutorizada.getMorador().getId(),
                pessoaAutorizada.getMorador().getPessoa().getNomeCompleto(),
                pessoaAutorizada.getMorador().getMoradia().getNumero()


        );
    }
}
