package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosConclusaoChamadoDTO(
        @NotBlank(message = "A resolução do chamado é obrigatória")
        @Size(max = 1000, message = "A resolução deve ter no máximo 1000 caracteres")
        String resolucaoChamado
) {
}
