package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoEspacoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarEspacoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaEspacoDTO;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.services.EspacoCondominialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/espacos")
public class EspacoCondominialController {

    @Autowired
    private EspacoCondominialService espacoCondominialService;


    @GetMapping
    public ResponseEntity<List<DadosConsultaEspacoDTO>> listarEspacosAtivos(){
        return ResponseEntity.ok(espacoCondominialService.listarEspacosCondominiais());
    }


    @GetMapping("/inativos")
    public ResponseEntity<List<DadosConsultaEspacoDTO>> listarEspacosNaoAtivos() {
        return ResponseEntity.ok(espacoCondominialService.listarEspacosCondominiaisInativos());
    }

    @GetMapping("/inativos/{id}")
    public ResponseEntity<DadosConsultaEspacoDTO> encontrarEspacoInativoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(espacoCondominialService.buscarEspacoInativoPorId(id));
    }


    @GetMapping("/{id}")
    public ResponseEntity<DadosConsultaEspacoDTO> encontrarEspacoPorId(@PathVariable Long id) {
        return ResponseEntity.ok(espacoCondominialService.buscarEspacoId(id));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaEspacoDTO> registrarEspaco(
            @Valid @ModelAttribute DadosRegistrarEspacoDTO dados,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal Usuario logado,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        DadosConsultaEspacoDTO espacoDTO =
                espacoCondominialService.registrarEspacoCondominial(dados, logado, fotoRecebida);

        URI uri = uriBuilder.path("/espacos/{id}").buildAndExpand(espacoDTO.idEspacoCondominial()).toUri();

        return ResponseEntity.created(uri).body(espacoDTO);
    }


    @PatchMapping("/inativos/reativar/{id}")
    public ResponseEntity<Void> reativarEspaco(
            @PathVariable Long id) {

        espacoCondominialService.reativarEspaco(id);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> editarEspaco(
            @PathVariable Long id,
            @Valid @ModelAttribute DadosAtualizacaoEspacoDTO dados,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {


        MultipartFile fotoRecebida = (arquivo != null && !arquivo.isEmpty()) ? arquivo : foto;
        espacoCondominialService.editarEspacoCondominial(id, dados, fotoRecebida);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarPorteiroPorId(@PathVariable Long id) {
        espacoCondominialService.desativarEspacos(id);
        return ResponseEntity.noContent().build();
    }
}
