package com.domus.tcc.backend.services;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.domus.tcc.backend.dto.request.DadosAtualizacaoEncomendaDTO;
import com.domus.tcc.backend.dto.request.DadosAtualizarStatusEncomendaDTO;
import com.domus.tcc.backend.domain.enums.StatusEncomenda;
import com.domus.tcc.backend.dto.request.DadosEnvioEmailDTO;
import com.domus.tcc.backend.util.EncomendaRegistradaEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domus.tcc.backend.dto.response.DadosConsultaEncomendaDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarEncomendaDTO;
import com.domus.tcc.backend.domain.Encomenda;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.repository.EncomendaRepository;
import com.domus.tcc.backend.repository.PessoaRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PortariaService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EncomendaRepository encomendaRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    private String gerarTokenEncomenda(){
        SecureRandom random = new SecureRandom();
        int tokenEncomenda = random.nextInt(10000);
        return String.format("%04d", tokenEncomenda);
    }

    @PreAuthorize("hasRole('PORTEIRO')")
    @Transactional
    public DadosConsultaEncomendaDTO registrarEncomenda(DadosRegistrarEncomendaDTO dados, Usuario logado) {
        Pessoa pessoa = pessoaRepository.findById(dados.idDestinatario())
                .orElseThrow(() -> new EntityNotFoundException("Morador não encontrado"));

        var porteiro = usuarioRepository.findById(logado.getId())
            .orElseThrow(() -> new EntityNotFoundException("Porteiro não encontrado"));


        var encomenda = new Encomenda(dados, porteiro, pessoa, gerarTokenEncomenda());
        encomendaRepository.save(encomenda);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        applicationEventPublisher.publishEvent(new EncomendaRegistradaEvent(
                pessoa.getNomeCompleto(),
                pessoa.getEmail(),
                dados.nomePacote(),
                porteiro.getPessoa().getNomeCompleto(),
                LocalDateTime.now().format(formatter)
        ));

        return new DadosConsultaEncomendaDTO(encomenda);
    }

    @Transactional(readOnly = true)
    public List<DadosConsultaEncomendaDTO> listarEncomendas(StatusEncomenda status) {
        List<Encomenda> encomendas = (status != null)
                ? encomendaRepository.findByStatus(status)
                : encomendaRepository.findAll();

        return encomendas.stream()
                .map(DadosConsultaEncomendaDTO::new)
                .toList();
    }


    @Transactional(readOnly = true)
    public DadosConsultaEncomendaDTO buscarEncomendaPorId(Long id) {
        var encomenda = encomendaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Encomenda não encontrada"));
        return new DadosConsultaEncomendaDTO(encomenda);
    }


    @Transactional
    public void registrarEntregaEncomenda(Long idEncomenda, DadosAtualizarStatusEncomendaDTO dados) {

        var encomenda = encomendaRepository.findById(idEncomenda)
                .orElseThrow(() -> new RuntimeException("Encomenda não encontrada!"));

        if (encomenda.getStatusEncomenda() == StatusEncomenda.ENTREGUE) {
            throw new RuntimeException("Esta encomenda já consta como Retirada!");
        }

        encomenda.setStatusEncomenda(StatusEncomenda.ENTREGUE);
        encomenda.setDataHoraRetirado(LocalDateTime.now());
        encomenda.setTipoRetirada(dados.tipoRetirada());

        encomendaRepository.save(encomenda);
    }

    @Transactional
    public void editarEncomenda(Long id, DadosAtualizacaoEncomendaDTO dados) {

        var encomenda = encomendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Encomenda não encontrada!"));


        if (dados.observacao() != null) {
            encomenda.setObservacao(dados.observacao());
        }

        if (dados.nomePacote() != null) {
            encomenda.setNomePacote(dados.nomePacote());
        }

        if (dados.idDestinatario() != null) {

            // Busca a nova pessoa no banco
            var novoDestinatario = pessoaRepository.findById(dados.idDestinatario())
                    .orElseThrow(() -> new RuntimeException("Novo destinatário não encontrado no sistema!"));

            encomenda.setMoradorDestinatario(novoDestinatario);
        }



        encomendaRepository.save(encomenda);
    }


    @Transactional
    public void deletarEncomendaPorId(Long id){
        if (!encomendaRepository.existsById(id)) {
            throw new EntityNotFoundException("A encomenda informada não existe.");
        }
        encomendaRepository.deleteById(id);
    }
}