package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoPorteiroDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarPorteiroDTO;
import com.domus.tcc.backend.dto.response.*;
import com.domus.tcc.backend.services.SindicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/consultacondominial")
public class ConsultaCondominialController {

    @Autowired
    private SindicoService sindicoService;

    // Busca todas moradias
    @GetMapping("/moradias")
    public ResponseEntity<List<DadosConsultaMoradiaDTO>> listarMoradias() {
        return ResponseEntity.ok(sindicoService.listarTodasMoradias());
    }

    // Busca uma moradia pelo seu id
    @GetMapping("/moradias/{id}")
    public ResponseEntity<DadosConsultaMoradiaDTO> encontrarMoradiaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.buscarMoradiaPorId(id));
    }

    // Busca uma moradores de uma moradia
    @GetMapping("/moradias/{id}/moradores")
    public ResponseEntity<DadosConsultaMoradiaMoradoresDTO> encontrarMoradorPorIdMoradia(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.listarMoradoresPorMoradiaId(id));
    }


    // Busca todos os blocos
    @GetMapping("/blocos")
    public ResponseEntity<List<DadosConsultaBlocoDTO>> listarBlocos() {
        return ResponseEntity.ok(sindicoService.listarTodosBlocos());
    }

    // Busca um bloco pelo seu id
    @GetMapping("/blocos/{id}")
    public ResponseEntity<DadosConsultaBlocoDTO> encontrarBlocoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.buscarBlocoPorId(id));
    }

    // Busca uma moradias de um bloco
    @GetMapping("/blocos/{id}/moradias")
    public ResponseEntity<DadosConsultaBlocoMoradiasDTO> encontrarMoradiasPorIdBloco(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.listarMoradiasPorBlocoId(id));
    }
}
