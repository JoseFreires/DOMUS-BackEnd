package com.domus.tcc.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.domus.tcc.backend.dto.request.DadosNovaSenhaDTO;
import com.domus.tcc.backend.dto.request.DadosSolicitacaoRedefinicaoSenhaDTO;
import com.domus.tcc.backend.services.RedefinicaoSenhaService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class RedefinicaoSenhaController {

    private final RedefinicaoSenhaService redefinicaoSenhaService;

    public RedefinicaoSenhaController(RedefinicaoSenhaService redefinicaoSenhaService) {
        this.redefinicaoSenhaService = redefinicaoSenhaService;
    }

    @PostMapping("/esqueci-minha-senha")
    public ResponseEntity<Map<String, String>> solicitarRedefinicao(@RequestBody @Valid DadosSolicitacaoRedefinicaoSenhaDTO dados) {
        try {
            redefinicaoSenhaService.solicitarRedefinicao(dados.email());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.accepted().body(Map.of(
                "message", "Se o e-mail estiver cadastrado, você receberá as instruções para redefinir sua senha."
        ));
    }

    @PutMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(
            @RequestParam("token") String token,
            @RequestBody @Valid DadosNovaSenhaDTO dados) {
        try {
            redefinicaoSenhaService.redefinirSenha(token, dados);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }
}
