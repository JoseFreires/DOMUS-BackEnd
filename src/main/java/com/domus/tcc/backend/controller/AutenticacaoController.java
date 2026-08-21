package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.response.DadosConsultaLoginDTO;
import com.domus.tcc.backend.dto.DadosLoginDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.domain.ContaAdm;
import com.domus.tcc.backend.services.TokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;


    //Método para não repetir a lógica de contaAdm e Usuario
    private DadosConsultaLoginDTO extrairDadosUsuario(Object principal) {

        if (principal instanceof Usuario usuario) {
            String role = usuario.getAuthorities().iterator().next().getAuthority();
            return new DadosConsultaLoginDTO(
                    usuario.getId(),
                    usuario.getUsername(),
                    usuario.getPessoa().getNomeCompleto(),
                    role
            );

        } else if (principal instanceof ContaAdm contaAdm) {
            String role = contaAdm.getAuthorities().iterator().next().getAuthority();
            return new DadosConsultaLoginDTO(
                    contaAdm.getIdContaAdm(),
                    contaAdm.getUsername(),
                    contaAdm.getNomeConta(),
                    role
            );

        } else {
            throw new ClassCastException("Tipo de usuário não reconhecido na extração: " + principal.getClass());
        }
    }


    //POST Login
    @PostMapping("/entrar")
    public ResponseEntity<DadosConsultaLoginDTO> entrar(@RequestBody @Valid DadosLoginDTO dados) {

        var token = new UsernamePasswordAuthenticationToken(dados.username(), dados.senha());
        var authentication = manager.authenticate(token);
        var principal = authentication.getPrincipal();


        String tokenJwt;
        if (principal instanceof Usuario usuario) {
            tokenJwt = tokenService.gerarTokenUsuario(usuario);
        } else if (principal instanceof ContaAdm contaAdm) {
            tokenJwt = tokenService.gerarTokenContaAdm(contaAdm);
        } else {
            throw new ClassCastException("Tipo de usuário não reconhecido: " + principal.getClass());
        }


        DadosConsultaLoginDTO dadosFrontEnd = extrairDadosUsuario(principal);

        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", tokenJwt)
                .httpOnly(true)
                .secure(false) // OBS: mudar para true em produção
                .path("/")
                .maxAge(2 * 60 * 60)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(dadosFrontEnd);
    }

    //POST Login
    @PostMapping("/sair")
    public ResponseEntity<Void> sair() {
        ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(false) // OBS: mudar para true em produção
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .build();
    }


    //Como solicitado pelos "bons de frente" GET login
    @GetMapping("/eu")
    public ResponseEntity<DadosConsultaLoginDTO> consultaUsuarioLogado(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var principal = authentication.getPrincipal();
        DadosConsultaLoginDTO dadosFrontEnd = extrairDadosUsuario(principal);

        return ResponseEntity.ok(dadosFrontEnd);
    }
}