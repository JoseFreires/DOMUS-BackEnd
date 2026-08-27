package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoMoradiaDTO(


        @NotBlank(message = "O número da moradia é obrigatório")
        @Size(max = 20, message = "O número da moradia não pode ter mais de 20 caracteres")
        String numero

) {
}
