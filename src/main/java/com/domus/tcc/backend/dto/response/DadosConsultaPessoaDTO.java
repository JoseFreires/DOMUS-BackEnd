package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.security.Usuario;

import java.time.LocalDate;

public record DadosConsultaPessoaDTO(

        Long idUsuario,

        String nomeCompleto,

        String cpf,


        String email,


        String telefone,


        LocalDate dataNascimento,

        Boolean ativo,

        String fotoPerfil
)
{
        public DadosConsultaPessoaDTO(Usuario usuario){
            this(
            usuario.getId(),
            usuario.getPessoa().getNomeCompleto(),
            usuario.getPessoa().getCpf(),
            usuario.getPessoa().getEmail(),
            usuario.getPessoa().getTelefone(),
            usuario.getPessoa().getDataNascimento(),
            usuario.getPessoa().getAtivo(),
            usuario.getPessoa().getFotoPerfil()
            );
        }

}
