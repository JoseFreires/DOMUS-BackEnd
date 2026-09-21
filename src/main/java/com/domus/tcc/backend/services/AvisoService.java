package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.AvisoCondominial;
import com.domus.tcc.backend.domain.Condominio;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoAvisoDTO;
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

import java.time.LocalDate;
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        var sindico = usuarioRepository.findById(logado.getId())
                .orElseThrow(() -> new EntityNotFoundException("Sindico não encontrado"));


        var condominio = condominioRepository.findById(1L)
                .orElseThrow(() -> new EntityNotFoundException("Condominio não encontrado"));



        String fotoUrl = null;
        if (arquivo != null && !arquivo.isEmpty()) {
            try {
                fotoUrl = fotoService.uploadFotoAviso(arquivo);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar foto do aviso: " + e.getMessage(), e);
            }
        }

         if (!dados.dataValidadeAviso().isAfter(LocalDateTime.now().toLocalDate())) {
            throw new IllegalArgumentException("A data de validade não pode ser inferior ou igual à data atual.");
        }

        var aviso = new AvisoCondominial(dados, fotoUrl, sindico, condominio);
        avisosRepository.save(aviso);


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

    @Transactional
    public int expirarAvisosVencidos() {
        return avisosRepository.expirarAvisoVencidos(LocalDate.now());
    }

    //GET por id
    @Transactional(readOnly = true)
    public DadosConsultaAvisoDTO buscarAvisoPorId(Long idAviso) {
        AvisoCondominial aviso = avisosRepository.findById(idAviso)
                .orElseThrow(() -> new EntityNotFoundException("Aviso não encontrado"));

        return new DadosConsultaAvisoDTO(aviso);
    }


    //GET lista avisos ativos
    @Transactional(readOnly = true)
    public List<DadosConsultaAvisoDTO> listarTodosAvisosCondominiais() {
        return avisosRepository.findAvisosAtivos(LocalDateTime.now().toLocalDate())
                .stream()
                .map(DadosConsultaAvisoDTO::new)
                .toList();
    }


    //GET lista de avisos não ativos
    @Transactional(readOnly = true)
    public List<DadosConsultaAvisoDTO> listarTodosAvisosNaoAtivos() {
        return avisosRepository.findAvisosNaoAtivos()
                .stream()
                .map(DadosConsultaAvisoDTO::new)
                .toList();
    }

    //PUT avisos
    @Transactional
    public DadosConsultaAvisoDTO atualizarAviso(Long id, DadosAtualizacaoAvisoDTO dados, MultipartFile novoArquivo) {

        AvisoCondominial aviso = avisosRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Aviso não encontrado"));

        if (dados.titulo() != null) {
            aviso.setTitulo(dados.titulo());
        }

        if (dados.descricao() != null) {
            aviso.setDescricao(dados.descricao());
        }

        if (dados.prioridade() != null) {
            aviso.setPrioridade(dados.prioridade());
        }

        if (dados.tipoAviso() != null) {
            aviso.setTipoAviso(dados.tipoAviso());
        }

        if (dados.dataValidadeAviso() != null) {
            aviso.setDataValidadeAviso(dados.dataValidadeAviso());
        }

        if (novoArquivo != null && !novoArquivo.isEmpty()) {
            try {

                if (aviso.getFotoAviso() != null && !aviso.getFotoAviso().isBlank()) {
                    fotoService.deletarFotoAviso(aviso.getFotoAviso());
                }

                String novaFotoUrl = fotoService.uploadFotoAviso(novoArquivo);
                aviso.setFotoAviso(novaFotoUrl);

            } catch (Exception e) {
                throw new RuntimeException("Erro ao atualizar a foto do aviso: " + e.getMessage(), e);
            }
        }

        return new DadosConsultaAvisoDTO(aviso);
    }

    //Soft Delete
    @Transactional
    public void desativarAviso(Long id){

        AvisoCondominial aviso = avisosRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("aviso não encontrado."));

        aviso.setAtivo(false);

        avisosRepository.save(aviso);
    }

}
