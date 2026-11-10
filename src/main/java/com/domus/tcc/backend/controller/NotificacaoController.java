package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.response.DadosConsultaNotificacaoDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.services.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public ResponseEntity<List<DadosConsultaNotificacaoDTO>> listar(
            @AuthenticationPrincipal Usuario logado) {
        return ResponseEntity.ok(notificacaoService.listarDoUsuario(logado.getId()));
    }
}
