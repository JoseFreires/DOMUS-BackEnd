package com.domus.tcc.backend.dto.response;


import java.time.LocalDate;

import com.domus.tcc.backend.security.Usuario;

public record DadosConsultaMoradorDTO(
        Long idMorador,
        Long idPessoa,
        String nome,
        String email,
        String numeroApartamento,
        String bloco,
        String cpf,
        LocalDate dataChegada,
        LocalDate nascimento,

        String telefone,
        Boolean ativo
) {
    public DadosConsultaMoradorDTO(Usuario usuario) {
        this(
                usuario.getPessoa().getMorador().getId(),
                usuario.getPessoa().getId(),
                usuario.getPessoa().getNomeCompleto(),
                usuario.getPessoa().getEmail(),
                usuario.getPessoa().getMorador().getMoradia().getNumero(),
                usuario.getPessoa().getMorador().getMoradia().getBloco().getNome_torre(),
                usuario.getPessoa().getCpf(),
                usuario.getPessoa().getMorador().getDataChegada(),
                usuario.getPessoa().getDataNascimento(),
                usuario.getPessoa().getTelefone(),
                usuario.getPessoa().getAtivo()

        );

    }

}