package com.domus.tcc.backend.security;

import java.io.IOException;

import com.domus.tcc.backend.domain.ContaAdm;
import com.domus.tcc.backend.services.LogSistemaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class LogFilter extends OncePerRequestFilter {

    public static final String ACCESS_DENIED_ATTRIBUTE = LogFilter.class.getName() + ".accessDenied";

    private final LogSistemaService logService;

    public LogFilter(LogSistemaService logService) {
        this.logService = logService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || RotasPublicas.SWAGGER.equals(path)
                || path.startsWith(RotasPublicas.SWAGGER_PREFIX)
                || path.startsWith(RotasPublicas.OPENAPI_PREFIX);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            if (!response.isCommitted() && response.getStatus() < HttpServletResponse.SC_BAD_REQUEST) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            throw e;
        } finally {
            registrarLog(request, response);
        }
    }

    private void registrarLog(HttpServletRequest request, HttpServletResponse response) {
        Object principal = principalAtual();
        boolean login = RotasPublicas.ENTRAR.equals(request.getServletPath());
        boolean controller = request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) != null;
        boolean acessoNegado = response.getStatus() == HttpServletResponse.SC_FORBIDDEN
                && Boolean.TRUE.equals(request.getAttribute(ACCESS_DENIED_ATTRIBUTE));

        if (login || acessoNegado || (controller && deveLogar(principal))) {
            logService.salvarResposta(principal, request.getMethod(), request.getRequestURI(), response.getStatus());
        }
    }

    private Object principalAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getPrincipal();
    }

    private boolean deveLogar(Object principal) {
        if (principal instanceof Usuario usuario) {
            return usuario.getAuthorities().stream()
                    .noneMatch(authority -> authority.getAuthority().equals("ROLE_MORADOR"));
        }
        return principal instanceof ContaAdm;
    }
}
