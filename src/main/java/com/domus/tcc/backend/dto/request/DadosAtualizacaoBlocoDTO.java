package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoBlocoDTO(


        @NotBlank(message = "O nome da torre é obrigatório")
        @Size(max = 45, message = "O nome da torre não pode ter mais de 45 caracteres")
        String nomeTorre,

        @NotNull(message = "O ID do condomínio é obrigatório")
        Long condominioIdCondominio
) {
}
