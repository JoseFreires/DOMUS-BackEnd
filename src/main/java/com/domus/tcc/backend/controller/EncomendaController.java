package com.domus.tcc.backend.controller;

import java.net.URI;
import java.util.List;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoEncomendaDTO;
import com.domus.tcc.backend.dto.request.DadosAtualizarStatusEncomendaDTO;
import com.domus.tcc.backend.domain.enums.StatusEncomenda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import com.domus.tcc.backend.dto.response.DadosConsultaEncomendaDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarEncomendaDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.services.PortariaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/encomendas")
public class EncomendaController {

    @Autowired
    private PortariaService portariaService;

    @GetMapping
    public ResponseEntity<List<DadosConsultaEncomendaDTO>> listarEncomendas(
            @RequestParam(required = false) StatusEncomenda status) {

        return ResponseEntity.ok(portariaService.listarEncomendas(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosConsultaEncomendaDTO> encontrarEncomendaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(portariaService.buscarEncomendaPorId(id));
    }

    @PostMapping
    public ResponseEntity<DadosConsultaEncomendaDTO> registrarEncomenda(
            @Valid @RequestBody DadosRegistrarEncomendaDTO dados,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal Usuario logado) {

        DadosConsultaEncomendaDTO encomendaDto = portariaService.registrarEncomenda(dados, logado);
        URI uri = uriBuilder.path("/encomendas/{id}").buildAndExpand(encomendaDto.idEncomenda()).toUri();

        return ResponseEntity.created(uri).body(encomendaDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> editarEncomenda(
            @PathVariable Long id,
            @Valid @RequestBody DadosAtualizacaoEncomendaDTO dados) {

        portariaService.editarEncomenda(id, dados);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/entrega")
    public ResponseEntity<Void> registrarEntrega(
            @PathVariable Long id,
            @Valid @RequestBody DadosAtualizarStatusEncomendaDTO dados) {

        portariaService.registrarEntregaEncomenda(id, dados);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEncomendaPorId(@PathVariable Long id) {
        portariaService.deletarEncomendaPorId(id);
        return ResponseEntity.noContent().build();
    }

}