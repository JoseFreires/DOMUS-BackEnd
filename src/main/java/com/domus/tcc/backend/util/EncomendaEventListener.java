package com.domus.tcc.backend.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.domus.tcc.backend.services.EmailService;


@Component
public class EncomendaEventListener {

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;


    public EncomendaEventListener(
            EmailService emailService,
            SpringTemplateEngine templateEngine) {
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEncomendaRegistrada(EncomendaRegistradaEvent evento) {
        Context ctx = new Context();
        ctx.setVariable("nomeDestinatario", evento.nomeDestinatario());
        ctx.setVariable("nomePacote",       evento.nomePacote());
        ctx.setVariable("nomePorteiro",     evento.nomePorteiro());
        ctx.setVariable("dataChegada",      evento.dataChegada());

        String htmlBody = templateEngine.process("email-encomenda", ctx);

        emailService.enviarEmailHtml(
                evento.emailDestinatario(),
                "Sua encomenda chegou!",
                htmlBody
        );
    }

}
