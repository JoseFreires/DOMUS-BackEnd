package com.domus.tcc.backend.services;

import java.util.List;

import com.domus.tcc.backend.dto.request.DadosEnvioEmailDTO;

public interface EmailService {
    void enviarEmail(DadosEnvioEmailDTO dados);
    void enviarEmailHtml(String destinatario, String assunto, String htmlBody);
    default void enviarEmailHtml(String remetente, String destinatario, String assunto, String htmlBody) {
        enviarEmailHtml(destinatario, assunto, htmlBody);
    }
    void enviarBroadcast(List<String> moradores, String assunto, String htmlBody);
}
