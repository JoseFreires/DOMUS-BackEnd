package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.domain.enums.StatusChamado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizarStatusChamadoDTO(
        @NotNull(message = "O status do chamado é obrigatório")
        StatusChamado statusChamado,

        @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres")
        String descricao
) {
}
