package com.domus.tcc.backend.controller;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoAvisoDTO;
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
import org.springframework.web.util.UriComponentsBuilder;
import com.domus.tcc.backend.services.AvisoService;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/avisos")
public class AvisoController {

    @Autowired
    private AvisoService avisoService;

    @GetMapping
    public ResponseEntity<List<DadosConsultaAvisoDTO>> listarAvisosAtivos(){
        return ResponseEntity.ok(avisoService.listarTodosAvisosCondominiais());
    }

    @GetMapping("/desativados")
    public ResponseEntity<List<DadosConsultaAvisoDTO>> listarAvisosNaoAtivos() {
        return ResponseEntity.ok(avisoService.listarTodosAvisosNaoAtivos());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosConsultaAvisoDTO> registrarAviso(
            @Valid @ModelAttribute DadosRegistrarAvisoDTO dados,
            UriComponentsBuilder uriBuilder,
            @AuthenticationPrincipal Usuario logado,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        DadosConsultaAvisoDTO avisoDTO =
                avisoService.registrarAvisoCondominial(dados,logado, fotoRecebida);

        URI uri = uriBuilder.path("/avisos/{id}").buildAndExpand(avisoDTO.idAviso()).toUri();

        return ResponseEntity.created(uri).body(avisoDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> editarAviso(
            @PathVariable Long id,
            @Valid @ModelAttribute DadosAtualizacaoAvisoDTO dados,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto) {

        MultipartFile fotoRecebida = (arquivo != null && !arquivo.isEmpty()) ? arquivo : foto;
        avisoService.atualizarAviso(id, dados, fotoRecebida);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarAviso(@PathVariable Long id) {
        avisoService.desativarAviso(id);
        return ResponseEntity.noContent().build();
    }

}
