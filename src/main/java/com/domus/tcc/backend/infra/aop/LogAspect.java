package com.domus.tcc.backend.infra.aop;

import com.domus.tcc.backend.domain.ContaAdm;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.services.LogSistemaService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Slf4j
@Component
public class LogAspect {

    private final LogSistemaService logService;
    private final HttpServletRequest request;

    public LogAspect(LogSistemaService logService, HttpServletRequest request) {
        this.logService = logService;
        this.request = request;
    }

    @Before("execution(* com.hermes..controller..*(..))")
    public void registrarLog(JoinPoint joinPoint) {

        String path = request.getRequestURI();

        if (path.equals("/login")) {
            return;
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        var principal = auth.getPrincipal();

        // Morador não gera log
        if (principal instanceof Usuario usuario) {
            boolean ehMorador = usuario.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MORADOR"));

            if (ehMorador) {
                return;
            }

            logService.salvarPorUsuario(usuario, request.getMethod(), path);

        } else if (principal instanceof ContaAdm contaAdm) {
            logService.salvarPorContaAdm(contaAdm, request.getMethod(), path);
        }
    }
}