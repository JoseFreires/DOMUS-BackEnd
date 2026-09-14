package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosRegistrarAvisoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaAvisoDTO;
import com.domus.tcc.backend.security.Usuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import com.domus.tcc.backend.services.AvisoService;
import java.net.URI;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/avisos")
public class AvisoController {

    @Autowired
    private AvisoService avisoService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaAvisoDTO> registrarAviso(
            @Valid @ModelAttribute DadosRegistrarAvisoDTO dados,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal Usuario logado,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        if (fotoRecebida == null || fotoRecebida.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A foto da encomenda é obrigatória (campo 'foto' ou 'arquivo')");
        }

        DadosConsultaAvisoDTO avisoDTO =
                avisoService.registrarAvisoCondominial(dados,logado, fotoRecebida);

        URI uri = uriBuilder.path("/avisos/{id}").buildAndExpand(avisoDTO.idAviso()).toUri();

        return ResponseEntity.created(uri).body(avisoDTO);
    }
}
