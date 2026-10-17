package com.domus.tcc.backend.util;

import java.time.LocalDateTime;

public record EmailNotificacaoEvent(
        String destinatario,
        String assunto,
        String mensagem,
        LocalDateTime dataHora
) {
}
