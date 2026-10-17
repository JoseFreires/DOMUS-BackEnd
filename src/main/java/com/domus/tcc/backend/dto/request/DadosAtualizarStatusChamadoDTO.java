package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.domain.enums.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record DadosAtualizarStatusChamadoDTO(
        @NotNull(message = "O status do chamado é obrigatório")
        StatusChamado statusChamado
) {
}
