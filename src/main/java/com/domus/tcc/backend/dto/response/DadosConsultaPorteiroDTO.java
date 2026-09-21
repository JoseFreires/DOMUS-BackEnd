package com.domus.tcc.backend.dto.response;

import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.domain.enums.TurnoPorteiro;

import java.time.LocalDate;
import java.util.Date;

public record DadosConsultaPorteiroDTO(

        Long idPorteiro,

        Long idUsuario,

        TurnoPorteiro turno,

        String empresaResponsavel,

        Long pessoaIdPessoa,

        String nomeCompleto,

        String cpf,

        String email,

        String telefone,

        LocalDate dataNascimento,

        String fotoPerfil

) {// Construtor
    public DadosConsultaPorteiroDTO(Usuario usuario) {
        this(
                usuario.getPessoa().getPorteiro().getId(),
                usuario.getId(),
                usuario.getPessoa().getPorteiro().getTurno(),
                usuario.getPessoa().getPorteiro().getEmpresaResponsavel(),
                usuario.getPessoa().getId(),
                usuario.getPessoa().getNomeCompleto(),
                usuario.getPessoa().getCpf(),
                usuario.getPessoa().getEmail(),
                usuario.getPessoa().getTelefone(),
                usuario.getPessoa().getDataNascimento(),
                usuario.getPessoa().getFotoPerfil()

        );
    }
}
