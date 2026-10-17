package com.domus.tcc.backend.util;

import com.domus.tcc.backend.services.EmailService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.format.DateTimeFormatter;

@Component
public class EmailNotificacaoEventListener {

    private static final String TEMPLATE_NOTIFICACAO = "email-notificacao";
    private static final DateTimeFormatter FORMATTER_DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;

    public EmailNotificacaoEventListener(
            EmailService emailService,
            SpringTemplateEngine templateEngine) {
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviar(EmailNotificacaoEvent evento) {
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariable("assunto", evento.assunto());
        context.setVariable("mensagem", evento.mensagem());
        context.setVariable("dataHora", evento.dataHora().format(FORMATTER_DATA_HORA));

        String html = templateEngine.process(TEMPLATE_NOTIFICACAO, context);
        emailService.enviarEmailHtml(
                evento.destinatario(),
                evento.assunto(),
                html);
    }
}
