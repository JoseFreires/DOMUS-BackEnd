package com.domus.tcc.backend.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.domus.tcc.backend.dto.request.DadosEnvioEmailDTO;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String usuarioSmtp;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void enviarEmail(DadosEnvioEmailDTO dados) {
        try {
            String remetente = (dados.remetente() == null || dados.remetente().isBlank())
                    ? usuarioSmtp
                    : dados.remetente();

            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo(dados.destinatario());
            mensagem.setSubject(dados.assunto());
            mensagem.setText(dados.corpo());
            mailSender.send(mensagem);
            logger.info("Email enviado para {}", dados.destinatario());
        } catch (Exception e) {
            logger.error("Falha ao enviar email para {}: {}", dados.destinatario(), e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarEmailHtml(String destinatario, String assunto, String htmlBody) {
        enviarEmailHtml(usuarioSmtp, destinatario, assunto, htmlBody);
    }

    @Override
    @Async
    public void enviarEmailHtml(String remetente, String destinatario, String assunto, String htmlBody) {
        try {
            String remetenteFinal = (remetente == null || remetente.isBlank()) ? usuarioSmtp : remetente;

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(remetenteFinal);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);
            logger.info("Email HTML enviado para {}", destinatario);
        } catch (MessagingException e) {
            logger.error("Falha ao enviar email HTML para {}: {}", destinatario, e.getMessage());
        }
    }

    @Async
    public void enviarBroadcast(List<String> moradores, String assunto, String htmlBody) {
        int deuBom = 0, deuRuim = 0;
        for (String morador : moradores) {
            try {
                enviarEmailHtml(morador, assunto, htmlBody);
                deuBom++;
            } catch (Exception e) {
                deuRuim++;
                logger.error("Erro no broadcast para {}: {}", morador, e.getMessage());
            }
        }
        logger.info("Broadcast finalizado. deuBom: {}, deuRuim: {}", deuBom, deuRuim);
    }
}