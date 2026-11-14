package com.domus.tcc.backend.security;

import com.domus.tcc.backend.domain.ContaAdm;
import com.domus.tcc.backend.services.LogSistemaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogFilterTest {

    @Mock
    private LogSistemaService logService;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraStatusDeLoginSemPrincipal() throws Exception {
        var filter = new LogFilter(logService);
        var request = new MockHttpServletRequest("POST", RotasPublicas.ENTRAR);
        request.setServletPath(RotasPublicas.ENTRAR);
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
        });

        verify(logService).salvarResposta(null, "POST", RotasPublicas.ENTRAR, 200);
    }

    @Test
    void registraAcessoNegadoMesmoSemController() throws Exception {
        var filter = new LogFilter(logService);
        var request = new MockHttpServletRequest("GET", "/restrito");
        request.setAttribute(LogFilter.ACCESS_DENIED_ATTRIBUTE, true);
        var response = new MockHttpServletResponse();
        response.setStatus(403);

        filter.doFilter(request, response, (req, res) -> {
        });

        verify(logService).salvarResposta(null, "GET", "/restrito", 403);
    }

    @Test
    void naoRegistraAcaoNormalDeMorador() throws Exception {
        var filter = new LogFilter(logService);
        var morador = mock(Usuario.class);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_MORADOR"));
        doReturn(authorities).when(morador).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(morador, null, authorities));

        var request = new MockHttpServletRequest("GET", "/avisos");
        request.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, new Object());
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
        });

        verifyNoInteractions(logService);
    }

    @Test
    void registraMoradorNoAcessoNegadoComoAntes() throws Exception {
        var filter = new LogFilter(logService);
        var morador = mock(Usuario.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(morador, null, List.of()));

        var request = new MockHttpServletRequest("GET", "/restrito");
        request.setAttribute(LogFilter.ACCESS_DENIED_ATTRIBUTE, true);
        var response = new MockHttpServletResponse();
        response.setStatus(403);

        filter.doFilter(request, response, (req, res) -> {
        });

        verify(logService).salvarResposta(morador, "GET", "/restrito", 403);
    }

    @Test
    void registraStatusFinalDoController() throws Exception {
        var filter = new LogFilter(logService);
        var admin = mock(ContaAdm.class);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, List.of()));

        var request = new MockHttpServletRequest("POST", "/avisos");
        request.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, new Object());
        var response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> response.setStatus(201));

        verify(logService).salvarResposta(admin, "POST", "/avisos", 201);
    }
}
