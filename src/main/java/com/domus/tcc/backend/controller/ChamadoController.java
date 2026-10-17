package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizarStatusChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarChamado;
import com.domus.tcc.backend.dto.response.DadosConsultaChamadoDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.services.ChamadoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaChamadoDTO> registrarChamado(
            @Valid @ModelAttribute DadosRegistrarChamado dados,
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
            @Valid @RequestBody DadosAtualizarStatusChamadoDTO dados) {

        return ResponseEntity.ok(chamadoService.atualizarStatusChamado(id, dados));
    }
}
