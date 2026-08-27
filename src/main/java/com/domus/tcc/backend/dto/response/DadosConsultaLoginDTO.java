package com.domus.tcc.backend.dto.response;

public record DadosConsultaLoginDTO(
        Long id,
        String username,
        String nome,
        String role
) {
}
