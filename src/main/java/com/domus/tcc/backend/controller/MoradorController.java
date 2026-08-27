package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoMoradorDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarMoradorDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradorDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradorEncomendasDTO;
import com.domus.tcc.backend.services.SindicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/moradores")
public class MoradorController {

    @Autowired
    private SindicoService sindicoService;

    // Busca todos os moradores ativos
    @GetMapping
    public ResponseEntity<List<DadosConsultaMoradorDTO>> listarMoradores() {
        return ResponseEntity.ok(sindicoService.listarTodasMoradores());
    }

    // Busca um morador pelo seu id
    @GetMapping("/{id}")
    public ResponseEntity<DadosConsultaMoradorDTO> encontrarMoradorPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.buscarMoradorPorId(id));
    }

    // Busca as encomendas de um morador pelo seu id
    @GetMapping("/{id}/encomendas")
    public ResponseEntity<DadosConsultaMoradorEncomendasDTO> encontrarEncomendasMorador(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.buscarEncomendasMorador(id));
    }


    @PostMapping
    public ResponseEntity<DadosConsultaMoradorDTO> registrarMorador(@Valid @RequestBody DadosRegistrarMoradorDTO dados,
                                                             UriComponentsBuilder uriBuilder) {

        var moradorDto = sindicoService.registrarMorador(dados);

        var uri = uriBuilder.path("/moradores/{id}").buildAndExpand(moradorDto.idMorador()).toUri();

        return ResponseEntity.created(uri).body(moradorDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> editarMorador(
            @PathVariable Long id,
            @Valid @RequestBody DadosAtualizacaoMoradorDTO dados) {

        sindicoService.editarMorador(id, dados);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMoradorPorId(@PathVariable Long id) {
        sindicoService.desativarMorador(id);
        return ResponseEntity.noContent().build();
    }

}