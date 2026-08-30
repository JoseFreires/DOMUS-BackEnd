package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoPessoaDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarSindicoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaPessoaDTO;
import com.domus.tcc.backend.services.AdmService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
    public ResponseEntity<DadosConsultaPessoaDTO> registrarSindico(@Valid @ModelAttribute DadosRegistrarSindicoDTO dados,
                                                                   UriComponentsBuilder uriBuilder,
                                                                   @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
                                                                   @RequestParam(value = "foto", required = false) MultipartFile foto) {


        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        if (fotoRecebida == null || fotoRecebida.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A foto do porteiro é obrigatória (campo 'foto' ou 'arquivo')");
        }
        var sindicoDTO = admService.registrarSindico(dados, fotoRecebida);

        var uri = uriBuilder.path("/sindicos/{id}").buildAndExpand(sindicoDTO.idUsuario()).toUri();

        return ResponseEntity.created(uri).body(sindicoDTO);
    }

    // Atualiza o sindico
    @PutMapping("/{id}")
    public ResponseEntity<Void> editarSindico(
            @PathVariable Long id,
            @Valid @ModelAttribute DadosAtualizacaoPessoaDTO dados,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {


        MultipartFile fotoRecebida = (arquivo != null && !arquivo.isEmpty()) ? arquivo : foto;
        admService.editarSindico(id, dados, fotoRecebida);
        return ResponseEntity.noContent().build();
    }

    // Deleta o sindico
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarSindicoPorId(@PathVariable Long id) {
        admService.desativarSindico(id);
        return ResponseEntity.noContent().build();
    }

}
