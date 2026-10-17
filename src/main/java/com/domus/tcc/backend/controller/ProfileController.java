package com.domus.tcc.backend.controller;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoPerfilDTO;
import com.domus.tcc.backend.dto.response.DadosPerfilResponseDTO;
import com.domus.tcc.backend.security.JwtCookieUtil;
import com.domus.tcc.backend.services.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/auth/eu/perfil")
public class ProfileController {

    private final PerfilService perfilService;

    public ProfileController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping
    public ResponseEntity<DadosPerfilResponseDTO> consultarPerfil(Authentication authentication) {
        return ResponseEntity.ok(perfilService.consultarPerfil(authentication.getPrincipal()));
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DadosPerfilResponseDTO> alterarPerfil(
            Authentication authentication,
            @Valid @ModelAttribute DadosAtualizacaoPerfilDTO dados,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo,
            @RequestParam(value = "foto", required = false) MultipartFile foto
    ) {
        MultipartFile fotoRecebida = arquivo != null && !arquivo.isEmpty() ? arquivo : foto;
        PerfilService.AlteracaoPerfil alteracao =
                perfilService.alterarPerfil(authentication.getPrincipal(), dados, fotoRecebida);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, JwtCookieUtil.criar(alteracao.tokenJwt()).toString())
                .body(alteracao.perfil());
    }

    @DeleteMapping
    public ResponseEntity<Void> excluirPerfil(Authentication authentication) {
        perfilService.excluirPerfil(authentication.getPrincipal());
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, JwtCookieUtil.limpar().toString())
                .build();
    }
}
