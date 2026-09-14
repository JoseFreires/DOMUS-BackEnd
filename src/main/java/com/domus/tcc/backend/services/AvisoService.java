package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.AvisoCondominial;
import com.domus.tcc.backend.domain.Condominio;
import com.domus.tcc.backend.dto.request.DadosRegistrarAvisoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaAvisoDTO;
import com.domus.tcc.backend.repository.AvisosRepository;
import com.domus.tcc.backend.repository.CondominioRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.util.AvisoRegistradoEvent;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AvisoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AvisosRepository avisosRepository;

    @Autowired
    private CondominioRepository condominioRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private FotoService fotoService;

    //POST Bill
    @PreAuthorize("hasRole('SINDICO')")
    @Transactional
    public DadosConsultaAvisoDTO registrarAvisoCondominial(DadosRegistrarAvisoDTO dados, Usuario logado, MultipartFile arquivo) {

        var sindico = usuarioRepository.findById(logado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Sindico não encontrado"));


        var condominio = condominioRepository.findById(dados.idCondominio())
                .orElseThrow(() -> new EntityNotFoundException("Condominio não encontrado"));



        String fotoUrl = null;
        if (arquivo != null && !arquivo.isEmpty()) {
            try {
                fotoUrl = fotoService.uploadFotoAviso(arquivo);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar foto do aviso: " + e.getMessage(), e);
            }
        }

        var aviso = new AvisoCondominial(dados, fotoUrl, sindico, condominio);
        avisosRepository.save(aviso);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<String> emailsMoradores = condominio.getBlocos().stream()
                .flatMap(bloco -> bloco.getMoradias().stream())
                .flatMap(moradia -> moradia.getMoradores().stream())
                .map(morador -> morador.getPessoa().getEmail())
                .toList();

        applicationEventPublisher.publishEvent(new AvisoRegistradoEvent(
                emailsMoradores,
                dados.titulo(),
                sindico.getPessoa().getNomeCompleto(),
                LocalDateTime.now().format(formatter),
                dados.dataValidadeAviso().format(formatterData)

        ));

    return new DadosConsultaAvisoDTO(aviso);
    }
}
