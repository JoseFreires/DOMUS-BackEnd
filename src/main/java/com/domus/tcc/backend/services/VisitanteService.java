package com.domus.tcc.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.domus.tcc.backend.domain.Moradia;
import com.domus.tcc.backend.domain.Visitante;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoVisitanteDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarVisitanteDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaVisitanteDTO;
import com.domus.tcc.backend.repository.MoradiaRepository;
import com.domus.tcc.backend.repository.TipoVisitaRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.repository.VisitanteRepository;
import com.domus.tcc.backend.security.Usuario;

import jakarta.persistence.EntityNotFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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

        visitante.getMoradias().add(moradia);

        visitanteRepository.save(visitante);

        return new DadosConsultaVisitanteDTO(visitante, moradia);
    }

    /*
     * PUT: corrige os dados cadastrados do visitante.
     */
    @Transactional
    public DadosConsultaVisitanteDTO atualizarVisitante(
            Long id, DadosAtualizacaoVisitanteDTO dados) {

        if (dados.nome() == null && dados.idTipoVisita() == null && dados.idMoradia() == null) {
            throw new ResponseStatusException(
                    BAD_REQUEST, "Informe ao menos um campo para atualizar");
        }
        if (dados.nome() != null && !StringUtils.hasText(dados.nome())) {
            throw new ResponseStatusException(BAD_REQUEST, "O nome do visitante não pode estar vazio");
        }

        Visitante visitante = visitanteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Visitante não encontrado."));
        if (dados.nome() != null) {
            visitante.setNome(dados.nome());
        }
        if (dados.idTipoVisita() != null) {
            var tipoVisita = tipoVisitaRepository.findById(dados.idTipoVisita())
                    .orElseThrow(() -> new EntityNotFoundException("Tipo de visita não encontrado."));
            visitante.setTipoVisita(tipoVisita);
        }

        Moradia moradiaResposta;
        if (dados.idMoradia() != null) {
            moradiaResposta = moradiaRepository.findById(dados.idMoradia())
                    .orElseThrow(() -> new EntityNotFoundException("Moradia não encontrada."));
            if (!visitante.getMoradias().contains(moradiaResposta)) {
                visitante.getMoradias().add(moradiaResposta);
            }
        } else {
            moradiaResposta = visitante.getMoradias().stream()
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Visitante não possui moradia associada."));
        }

        visitanteRepository.save(visitante);
        return new DadosConsultaVisitanteDTO(visitante, moradiaResposta);
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