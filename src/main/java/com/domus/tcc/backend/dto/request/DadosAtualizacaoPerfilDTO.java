package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record DadosAtualizacaoPerfilDTO(
        @NotBlank String nomeCompleto,
        @NotBlank @Email String email,
        @NotBlank String telefone
) {
}
