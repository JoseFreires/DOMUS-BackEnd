package com.domus.tcc.backend.dto.request;

public record DadosEnvioEmailDTO(
        String remetente,
        String destinatario,
        String assunto,
        String corpo) {

    public DadosEnvioEmailDTO(String destinatario, String assunto, String corpo) {
        this(null, destinatario, assunto, corpo);
    }
}

