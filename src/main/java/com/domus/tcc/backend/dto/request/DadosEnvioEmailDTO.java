package com.domus.tcc.backend.dto.request;

public record DadosEnvioEmailDTO(
        String destinatario,
        String assunto,
        String corpo) {
}

