package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.domain.enums.TipoChamado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoChamadoDTO(
        @NotBlank(message = "O título do chamado é obrigatório")
        @Size(max = 45, message = "O título deve ter no máximo 45 caracteres")
        String titulo,

        @NotBlank(message = "A descrição do chamado é obrigatória")
        @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres")
        String descricao,

        @NotNull(message = "O tipo do chamado é obrigatório")
        TipoChamado tipoChamado
) {
}
