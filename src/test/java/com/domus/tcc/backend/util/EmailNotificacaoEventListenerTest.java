package com.domus.tcc.backend.util;

import com.domus.tcc.backend.services.EmailService;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailNotificacaoEventListenerTest {

    @Test
    void renderizaTemplateHtmlComDataHoraFormatada() {
        EmailService emailService = mock(EmailService.class);
        EmailNotificacaoEventListener listener =
                new EmailNotificacaoEventListener(emailService, criarTemplateEngine());

        listener.enviar(new EmailNotificacaoEvent(
                "morador@domus.com",
                "Encomenda retirada",
                "Sua encomenda foi retirada por terceiro.",
                LocalDateTime.of(2026, 10, 17, 0, 21)));

        org.mockito.ArgumentCaptor<String> htmlCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEmailHtml(
                eq("morador@domus.com"),
                eq("Encomenda retirada"),
                htmlCaptor.capture());

        String html = htmlCaptor.getValue();
        assertTrue(html.contains("17/10/2026 às 00:21"));
        assertTrue(html.contains("Sua encomenda foi retirada por terceiro."));
        assertTrue(html.contains("background-color: #003366"));
    }

    @Test
    void escapaConteudoDinamicoNoTemplate() {
        EmailService emailService = mock(EmailService.class);
        EmailNotificacaoEventListener listener =
                new EmailNotificacaoEventListener(emailService, criarTemplateEngine());

        listener.enviar(new EmailNotificacaoEvent(
                "morador@domus.com",
                "Aviso",
                "<script>alert('x')</script>",
                LocalDateTime.of(2026, 10, 17, 0, 21)));

        org.mockito.ArgumentCaptor<String> htmlCaptor =
                org.mockito.ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEmailHtml(eq("morador@domus.com"), eq("Aviso"), htmlCaptor.capture());

        assertTrue(htmlCaptor.getValue().contains("&lt;script&gt;"));
        assertFalse(htmlCaptor.getValue().contains("<script>alert"));
    }

    private SpringTemplateEngine criarTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
        return templateEngine;
    }
}
