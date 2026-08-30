package com.domus.tcc.backend.dto.request;

import com.domus.tcc.backend.dto.DadosLoginDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DadosRegistrarMoradorDTO(

        @NotNull @Valid
        DadosRegistrarPessoaDTO pessoa,

        @NotNull @Valid
        DadosLoginDTO usuario,

        @NotNull(message = "O ID da moradia/apartamento é obrigatório")
        Long idMoradia,

        @NotNull(message = "A data de chegada ao condomínio é obrigatória")
        LocalDate dataChegada
) {
}