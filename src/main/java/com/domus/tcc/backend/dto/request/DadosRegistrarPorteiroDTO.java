package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.dto.DadosLoginDTO;
import com.domus.tcc.backend.domain.enums.TurnoPorteiro;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DadosRegistrarPorteiroDTO(

        @NotNull(message = "O turno é obrigatório")
        TurnoPorteiro turno,

        @NotBlank(message = "A empresa responsável é obrigatória")
        @Size(max = 100, message = "A empresa responsável não pode ter mais de 100 caracteres")
        String empresaResponsavel,

        @NotNull @Valid
        DadosRegistrarPessoaDTO pessoa,

        @NotNull @Valid
        DadosLoginDTO usuario
) {
}
