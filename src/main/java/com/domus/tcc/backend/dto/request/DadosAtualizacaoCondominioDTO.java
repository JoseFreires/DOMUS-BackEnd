package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoCondominioDTO(


        @NotBlank(message = "O nome do condomínio é obrigatório")
        @Size(max = 120, message = "O nome do condomínio não pode ter mais de 120 caracteres")
        String nomeCondominio
) {
}
