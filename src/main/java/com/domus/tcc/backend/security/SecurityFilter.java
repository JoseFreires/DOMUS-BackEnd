package com.domus.tcc.backend.security;

import java.io.IOException;
import java.util.Set;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.domus.tcc.backend.repository.ContaAdmRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final ContaAdmRepository contaAdmRepository;
    private final TokenService tokenService;

    private static final Set<String> ROTAS_ISENTAS = Set.of(
            "POST " + RotasPublicas.ENTRAR,
            "POST " + RotasPublicas.SAIR,
            "GET " + RotasPublicas.USUARIO_ATUAL,
            "POST " + RotasPublicas.ESQUECI_SENHA,
            "PUT " + RotasPublicas.REDEFINIR_SENHA,
            "PUT " + RotasPublicas.ACEITAR_TERMOS
    );

    private boolean isRotaIsenta(HttpServletRequest request) {
        return ROTAS_ISENTAS.contains(request.getMethod().toUpperCase() + " " + request.getServletPath());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || RotasPublicas.SWAGGER.equals(path)
                || path.startsWith(RotasPublicas.SWAGGER_PREFIX)
                || path.startsWith(RotasPublicas.OPENAPI_PREFIX);
    }

    private void responderTermosNaoAceitos(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"codigo\":\"TERMOS_NAO_ACEITOS\",\"mensagem\":\"É necessário aceitar os termos de uso.\"}");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        String tokenJwt = recuperarToken(request);
        String subject = tokenJwt != null ? obterSubject(tokenJwt) : null;
        UserDetails principal = resolverPrincipal(subject);

        if (principal != null) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

            if (principal instanceof Usuario usuario
                    && !usuario.isTermosAceitos()
                    && !isRotaIsenta(request)) {
                request.setAttribute(LogFilter.ACCESS_DENIED_ATTRIBUTE, true);
                responderTermosNaoAceitos(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String obterSubject(String token) {
        try {
            return tokenService.getSubject(token);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof JWTVerificationException) {
                return null;
            }
            throw e;
        }
    }

    private UserDetails resolverPrincipal(String username) {
        if (username == null) {
            return null;
        }

        UserDetails principal = usuarioRepository.findByUsername(username);
        if (principal == null) {
            principal = contaAdmRepository.findByUsername(username);
        }
        return principal != null && principal.isEnabled() ? principal : null;
    }

    private String recuperarToken(HttpServletRequest request) {
        var cookie = WebUtils.getCookie(request, "jwtToken");
        return cookie != null ? cookie.getValue() : null;
    }
}