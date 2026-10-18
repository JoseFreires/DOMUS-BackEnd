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
import com.domus.tcc.backend.repository.TipoVisitaRepository;
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

    @Autowired
    private TipoVisitaRepository tipoVisitaRepository;

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

        var tipoVisita = tipoVisitaRepository.findById(dados.idTipoVisita())
                .orElseThrow(() ->
                        new EntityNotFoundException("Tipo de visita não encontrado."));

        var porteiro = usuarioRepository.findById(logado.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Porteiro não encontrado."));

        var visitante = new Visitante(dados, porteiro, tipoVisita);



        visitanteRepository.save(visitante);

        return new DadosConsultaVisitanteDTO(visitante, moradia);
    }

    /*
     * GET: retorna todos os visitantes cadastrados.
     */
    @Transactional(readOnly = true)
    public List<DadosConsultaVisitanteDTO> listarTodosVisitantes() {

        return visitanteRepository.findAll()
                .stream()
                .flatMap(visitante -> visitante.getMoradias().stream()
                        .map(moradia -> new DadosConsultaVisitanteDTO(visitante, moradia)))
                .toList();
    }
}