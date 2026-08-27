package com.domus.tcc.backend.dto.response;

import java.time.LocalDate;

import com.domus.tcc.backend.domain.Morador;

public record DadosConsultaMoradorResumoDTO(
        Long idMorador,
        Long idPessoa,
        String nome,
        String email,
        String cpf,
        LocalDate dataChegada,
        LocalDate nascimento,
        String telefone
) {
    public DadosConsultaMoradorResumoDTO(Morador morador) {
        this(
                morador.getId(),
                morador.getPessoa().getId(),
                morador.getPessoa().getNomeCompleto(),
                morador.getPessoa().getEmail(),
                morador.getPessoa().getCpf(),
                morador.getDataChegada(),
                morador.getPessoa().getDataNascimento(),
                morador.getPessoa().getTelefone()
        );
    }
}
