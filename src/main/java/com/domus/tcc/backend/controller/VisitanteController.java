package com.domus.tcc.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.domus.tcc.backend.dto.request.DadosRegistrarVisitanteDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaVisitanteDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.services.VisitanteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/visitantes")
public class VisitanteController {

    @Autowired
    private VisitanteService visitanteService;

    // Busca todos os visitantes
    @GetMapping
    public ResponseEntity<List<DadosConsultaVisitanteDTO>> listarVisitantes(
            @AuthenticationPrincipal Usuario logado) {

        return ResponseEntity.ok(
                visitanteService.listarTodosVisitantes()
        );
    }

    // Busca visitantes por moradia
    @GetMapping("/moradia/{idMoradia}")
    public ResponseEntity<List<DadosConsultaVisitanteDTO>> listarVisitantesPorMoradia(
            @PathVariable Long idMoradia,
            @AuthenticationPrincipal Usuario logado) {

        return ResponseEntity.ok(
                visitanteService.listarVisitantesPorMoradia(idMoradia)
        );
    }

    // Cria um novo visitante
    @PostMapping
    public ResponseEntity<DadosConsultaVisitanteDTO> registrarVisitante(
            @Valid @RequestBody DadosRegistrarVisitanteDTO dados,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal Usuario logado) {

        var visitanteDto = visitanteService.registrarVisitante(dados, logado);

        var uri = uriBuilder
                .path("/visitantes/{id}")
                .buildAndExpand(visitanteDto.idVisitante())
                .toUri();

        return ResponseEntity.created(uri).body(visitanteDto);
    }
}