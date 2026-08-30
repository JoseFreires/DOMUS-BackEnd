package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoPorteiroDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaPorteiroDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarPorteiroDTO;
import com.domus.tcc.backend.services.SindicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/porteiros")
public class PorteiroController {

    @Autowired
    private SindicoService sindicoService;

    // Busca todos os porteiros ativos
    @GetMapping
    public ResponseEntity<List<DadosConsultaPorteiroDTO>> listarPorteiros() {
        return ResponseEntity.ok(sindicoService.listarTodosPorteiros());
    }

    // Busca um porteiro pelo seu id
    @GetMapping("/{id}")
    public ResponseEntity<DadosConsultaPorteiroDTO> encontrarPorteiroPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.buscarPorteiroPorId(id));
    }

    // Cria um novo porteiro
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaPorteiroDTO> registrarPorteiro(@Valid @ModelAttribute DadosRegistrarPorteiroDTO dados,
                                                                     UriComponentsBuilder uriBuilder,
                                                                      @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
                                                                      @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        if (fotoRecebida == null || fotoRecebida.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A foto do porteiro é obrigatória (campo 'foto' ou 'arquivo')");
        }
        var porteiroDTO = sindicoService.registrarPorteiro(dados, fotoRecebida);

        var uri = uriBuilder.path("/porteiros/{id}").buildAndExpand(porteiroDTO.idUsuario()).toUri();

        return ResponseEntity.created(uri).body(porteiroDTO);
    }

    // Atualiza porteiro por Id
    @PutMapping("/{id}")
    public ResponseEntity<Void> editarPorteiro(
            @PathVariable Long id,
            @Valid @ModelAttribute DadosAtualizacaoPorteiroDTO dados,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {


        MultipartFile fotoRecebida = (arquivo != null && !arquivo.isEmpty()) ? arquivo : foto;
        sindicoService.editarPorteiro(id, dados, fotoRecebida);
        return ResponseEntity.noContent().build();
    }

    // Deletar porteiro por Id
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorteiroPorId(@PathVariable Long id) {
        sindicoService.desativarPorteiro(id);
        return ResponseEntity.noContent().build();
    }

}
