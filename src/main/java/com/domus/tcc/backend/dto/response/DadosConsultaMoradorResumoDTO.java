package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.domain.Morador;

import java.time.LocalDate;

public record DadosConsultaMoradorResumoDTO(
        Long idMorador,
        Long idPessoa,
        String nome,
        String email,
        String cpf,
        LocalDate dataChegada,
        LocalDate nascimento,
        String foto,
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
                morador.getUrlFoto(),
                morador.getPessoa().getTelefone()
        );
    }
}
