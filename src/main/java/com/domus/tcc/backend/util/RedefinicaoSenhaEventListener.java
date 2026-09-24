package com.domus.tcc.backend.util;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.domus.tcc.backend.services.EmailService;

@Component
public class RedefinicaoSenhaEventListener {

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;

    public RedefinicaoSenhaEventListener(EmailService emailService, SpringTemplateEngine templateEngine) {
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRedefinicaoSolicitada(RedefinicaoSenhaSolicitadaEvent evento) {
        Context ctx = new Context();
        ctx.setVariable("nomeUsuario", evento.nomeUsuario());
        ctx.setVariable("nomeSistema", evento.nomeSistema());
        ctx.setVariable("linkRedefinicao", evento.linkRedefinicao());

        String html = templateEngine.process("email-recuperacao-senha", ctx);
        emailService.enviarEmailHtml(evento.emailDestino(), "Redefina sua senha", html);
    }
}
