package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizarStatusChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosConclusaoChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarChamadoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaChamadoDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.services.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaChamadoDTO> registrarChamado(
            @Valid @ModelAttribute DadosRegistrarChamadoDTO dados,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal Usuario logado,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;

        DadosConsultaChamadoDTO chamadoDTO =
                chamadoService.registrarChamado(dados, logado, fotoRecebida);
        URI uri = uriBuilder.path("/chamados/{id}").buildAndExpand(chamadoDTO.idChamado()).toUri();

        return ResponseEntity.created(uri).body(chamadoDTO);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<DadosConsultaChamadoDTO> atualizarStatusChamado(
            @PathVariable Long id,
            @Valid @RequestBody DadosAtualizarStatusChamadoDTO dados,
            @AuthenticationPrincipal UserDetails logado) {

        return ResponseEntity.ok(chamadoService.atualizarStatusChamado(id, dados, logado));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<DadosConsultaChamadoDTO> concluirChamado(
            @PathVariable Long id,
            @Valid @RequestBody DadosConclusaoChamadoDTO dados,
            @AuthenticationPrincipal UserDetails logado) {

        return ResponseEntity.ok(chamadoService.concluirChamado(id, dados, logado));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaChamadoDTO> editarChamado(
            @PathVariable Long id,
            @Valid @ModelAttribute DadosAtualizacaoChamadoDTO dados,
            @AuthenticationPrincipal Usuario logado,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        return ResponseEntity.ok(chamadoService.editarChamado(id, dados, logado, fotoRecebida));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarChamado(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario logado) {
        chamadoService.cancelarChamado(id, logado);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<DadosConsultaChamadoDTO>> listarChamados(
            @AuthenticationPrincipal UserDetails logado) {
        return ResponseEntity.ok(chamadoService.listarChamados(logado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DadosConsultaChamadoDTO> encontrarChamadoPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails logado) {
        return ResponseEntity.ok(chamadoService.buscarChamadoPorId(id, logado));
    }
}
