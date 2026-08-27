package com.domus.tcc.backend.security;

import java.io.IOException;

import com.domus.tcc.backend.domain.ContaAdm;
import com.domus.tcc.backend.repository.ContaAdmRepository;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.domus.tcc.backend.services.TokenService;
import com.domus.tcc.backend.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final UsuarioRepository usuarioRepository;
    private final ContaAdmRepository contaAdmRepository;
    private final TokenService tokenService;

    public SecurityFilter(UsuarioRepository usuarioRepository,
                          ContaAdmRepository contaAdmRepository,
                          TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.contaAdmRepository = contaAdmRepository;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        String tokenJwt = recuperarToken(request);

        if (tokenJwt != null) {
            String subject = tokenService.getSubject(tokenJwt);

            UserDetails principal = resolverPrincipal(subject);

            if (principal != null) {
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails resolverPrincipal(String username) {
        Usuario usuario = (Usuario) usuarioRepository.findByUsername(username);
        if (usuario != null) {
            return usuario;
        }

        ContaAdm contaAdm = (ContaAdm) contaAdmRepository.findByUsername(username);
        if (contaAdm != null) {
            return contaAdm;
        }

        return null;
    }

    private String recuperarToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}