package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoMoradorDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarMoradorDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradorDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradorEncomendasDTO;
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
@RequestMapping("/moradores")
public class MoradorController {

    @Autowired
    private SindicoService sindicoService;

    @GetMapping
    public ResponseEntity<List<DadosConsultaMoradorDTO>> listarMoradores() {
        return ResponseEntity.ok(sindicoService.listarTodasMoradores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosConsultaMoradorDTO> encontrarMoradorPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.buscarMoradorPorId(id));
    }

    @GetMapping("/{id}/encomendas")
    public ResponseEntity<DadosConsultaMoradorEncomendasDTO> encontrarEncomendasMorador(@PathVariable Long id) {
        return ResponseEntity.ok(sindicoService.buscarEncomendasMorador(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaMoradorDTO> registrarMorador(
            @Valid @ModelAttribute DadosRegistrarMoradorDTO dados,
            UriComponentsBuilder uriBuilder,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        if (fotoRecebida == null || fotoRecebida.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A foto do morador é obrigatória (campo 'foto' ou 'arquivo')");
        }
        var moradorDto = sindicoService.registrarMorador(dados, fotoRecebida);

        var uri = uriBuilder.path("/moradores/{id}").buildAndExpand(moradorDto.idMorador()).toUri();

        return ResponseEntity.created(uri).body(moradorDto);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> editarMorador(
            @PathVariable Long id,
            @Valid @ModelAttribute DadosAtualizacaoMoradorDTO dados,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = (arquivo != null && !arquivo.isEmpty()) ? arquivo : foto;
        sindicoService.editarMorador(id, dados, fotoRecebida);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarMoradorPorId(@PathVariable Long id) {
        sindicoService.desativarMorador(id);
        return ResponseEntity.noContent().build();
    }
}