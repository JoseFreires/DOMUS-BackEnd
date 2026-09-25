package com.domus.tcc.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domus.tcc.backend.domain.Moradia;
import com.domus.tcc.backend.domain.Visitante;
import com.domus.tcc.backend.dto.request.DadosRegistrarVisitanteDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaVisitanteDTO;
import com.domus.tcc.backend.repository.MoradiaRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.repository.VisitanteRepository;
import com.domus.tcc.backend.security.Usuario;

import jakarta.persistence.EntityNotFoundException;

@Service
public class VisitanteService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VisitanteRepository visitanteRepository;

    @Autowired
    private MoradiaRepository moradiaRepository;

    /*
     * POST: registra um visitante e associa o visitante a uma moradia.
     */
    @PreAuthorize("hasRole('PORTEIRO')")
    @Transactional
    public DadosConsultaVisitanteDTO registrarVisitante(
            DadosRegistrarVisitanteDTO dados,
            Usuario logado) {

        Moradia moradia = moradiaRepository.findById(dados.idMoradia())
                .orElseThrow(() ->
                        new EntityNotFoundException("Moradia não encontrada."));

        var porteiro = usuarioRepository.findById(logado.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Porteiro não encontrado."));

        var visitante = new Visitante(dados, porteiro);

        visitante.getMoradias().add(moradia);

        visitanteRepository.save(visitante);

        return new DadosConsultaVisitanteDTO(visitante);
    }

    /*
     * GET: retorna todos os visitantes cadastrados.
     */
    @Transactional(readOnly = true)
    public List<DadosConsultaVisitanteDTO> listarTodosVisitantes() {

        return visitanteRepository.findAll()
                .stream()
                .map(DadosConsultaVisitanteDTO::new)
                .toList();
    }

    /*
     * GET: retorna os visitantes associados a uma moradia.
     */
    @Transactional(readOnly = true)
    public List<DadosConsultaVisitanteDTO> listarVisitantesPorMoradia(
            Long idMoradia) {

        if (!moradiaRepository.existsById(idMoradia)) {
            throw new EntityNotFoundException("Moradia não encontrada.");
        }

        return visitanteRepository.findByMoradiasId(idMoradia)
                .stream()
                .map(DadosConsultaVisitanteDTO::new)
                .toList();
    }
}