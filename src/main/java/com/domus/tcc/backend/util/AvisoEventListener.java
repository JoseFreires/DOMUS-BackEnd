package com.domus.tcc.backend.util;

import com.domus.tcc.backend.services.EmailService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
public class AvisoEventListener {

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;

    public AvisoEventListener(EmailService emailService, SpringTemplateEngine templateEngine) {
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAvisoRegistrada(AvisoRegistradoEvent evento) {
        Context ctx = new Context();
        ctx.setVariable("tituloAviso",   evento.tituloAviso());
        ctx.setVariable("nomeSindico",   evento.nomeSindico());
        ctx.setVariable("dataAbertura",  evento.dataAbertura());
        ctx.setVariable("dataValidade",  evento.dataValidade());

        String htmlBody = templateEngine.process("email-avisoCondominial", ctx);

        emailService.enviarBroadcast(
                evento.emailsDestinatarios(),
                "Aviso novo chegou!",
                htmlBody
        );
    }
}