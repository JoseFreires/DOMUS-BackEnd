package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoVisitanteDTO(
        @Pattern(regexp = "(?s).*\\S.*", message = "O nome do visitante não pode estar vazio.")
        @Size(max = 50, message = "O nome do visitante deve ter no máximo 50 caracteres.")
        String nome,

        Long idTipoVisita,

        Long idMoradia
) {
    @AssertTrue(message = "Informe ao menos um campo para atualizar.")
    public boolean isAlgumCampoInformado() {
        return nome != null || idTipoVisita != null || idMoradia != null;
    }
}
