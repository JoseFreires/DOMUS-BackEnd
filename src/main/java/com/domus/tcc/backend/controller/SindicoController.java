package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoPessoaDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarSindicoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaPessoaDTO;
import com.domus.tcc.backend.services.AdmService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/sindicos")
public class SindicoController {

    @Autowired
    AdmService admService;

    // Busca todos os sindicos ativos
    @GetMapping
    public ResponseEntity<List<DadosConsultaPessoaDTO>> listarSindicos() {
        return ResponseEntity.ok(admService.listarTodosSindicos());
    }

    // Busca um sindico pelo seu id
    @GetMapping("/{id}")
    public ResponseEntity<DadosConsultaPessoaDTO> encontrarSindicoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(admService.buscarSindicoPorId(id));
    }

    // Registra um novo sindico
    @PostMapping
    public ResponseEntity<DadosConsultaPessoaDTO> registrarSindico(@Valid @RequestBody DadosRegistrarSindicoDTO dados,
                                                                   UriComponentsBuilder uriBuilder) {

        var sindicoDTO = admService.registrarSindico(dados);

        var uri = uriBuilder.path("/sindicos/{id}").buildAndExpand(sindicoDTO.idUsuario()).toUri();

        return ResponseEntity.created(uri).body(sindicoDTO);
    }

    // Atualiza o sindico
    @PutMapping("/{id}")
    public ResponseEntity<Void> editarSindico(
            @PathVariable Long id,
            @Valid @RequestBody DadosAtualizacaoPessoaDTO dados) {

        admService.editarSindico(id, dados);
        return ResponseEntity.noContent().build();
    }

    // Deleta o sindico
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSindicoPorId(@PathVariable Long id) {
        admService.desativarSindico(id);
        return ResponseEntity.noContent().build();
    }

}
