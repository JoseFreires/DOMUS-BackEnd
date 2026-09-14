package com.domus.tcc.backend.services;

import com.domus.tcc.backend.dto.request.DadosEnvioEmailDTO;

import java.util.List;

public interface EmailService {
    void enviarEmail(DadosEnvioEmailDTO dados);
    void enviarEmailHtml(String destinatario, String assunto, String htmlBody);
    void enviarBroadcast(List<String> moradores, String assunto, String htmlBody);
}
