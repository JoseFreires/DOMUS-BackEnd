package com.domus.tcc.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DadosAtualizacaoPessoaAutorizadaDTO(


        @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres")
        String nome,

        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos")
        String cpf

) {
}
