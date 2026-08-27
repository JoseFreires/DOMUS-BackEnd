package com.domus.tcc.backend.services;

import com.domus.tcc.backend.dto.request.DadosEnvioEmailDTO;

public interface EmailService {
    void enviarEmail(DadosEnvioEmailDTO dados);
    void enviarEmailHtml(String destinatario, String assunto, String htmlBody);
}
