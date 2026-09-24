package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DadosNovaSenhaDTO(
        @NotBlank(message = "A nova senha é obrigatória.")
        String novaSenha,

        @NotBlank(message = "A confirmação da senha é obrigatória.")
        String confirmarSenha
) {}
