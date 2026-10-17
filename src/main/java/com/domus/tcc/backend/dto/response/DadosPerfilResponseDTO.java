package com.domus.tcc.backend.dto.response;

import java.time.LocalDate;

import com.domus.tcc.backend.domain.enums.TurnoPorteiro;

public sealed interface DadosPerfilResponseDTO permits
        DadosPerfilResponseDTO.Morador,
        DadosPerfilResponseDTO.Sindico,
        DadosPerfilResponseDTO.Porteiro,
        DadosPerfilResponseDTO.Administrador {

    String papel();

    record Morador(
            Long idUsuario,
            Long idMorador,
            String username,
            String nomeCompleto,
            String cpf,
            String email,
            String telefone,
            LocalDate dataNascimento,
            String fotoPerfil,
            LocalDate dataChegada,
            Long idMoradia,
            Long idBloco,
            String numeroApartamento,
            String bloco
    ) implements DadosPerfilResponseDTO {
        @Override
        public String papel() {
            return "MORADOR";
        }
    }

    record Sindico(
            Long idUsuario,
            String username,
            String nomeCompleto,
            String cpf,
            String email,
            String telefone,
            LocalDate dataNascimento,
            String fotoPerfil
    ) implements DadosPerfilResponseDTO {
        @Override
        public String papel() {
            return "SINDICO";
        }
    }

    record Porteiro(
            Long idUsuario,
            Long idPorteiro,
            String username,
            String nomeCompleto,
            String cpf,
            String email,
            String telefone,
            LocalDate dataNascimento,
            String fotoPerfil,
            TurnoPorteiro turno,
            String empresaResponsavel
    ) implements DadosPerfilResponseDTO {
        @Override
        public String papel() {
            return "PORTEIRO";
        }
    }

    record Administrador(
            Long idContaAdm,
            String email,
            String nomeConta
    ) implements DadosPerfilResponseDTO {
        @Override
        public String papel() {
            return "ADMIN";
        }
    }
}
