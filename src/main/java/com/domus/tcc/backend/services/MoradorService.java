package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.PessoaAutorizada;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoPessoaAutorizadaDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarPessoaAutorizadaDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaPessoaAutorizadaDTO;
import com.domus.tcc.backend.repository.MoradorRepository;
import com.domus.tcc.backend.repository.PessoaAutorizadaRepository;
import com.domus.tcc.backend.security.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MoradorService {

    @Autowired
    private MoradorRepository moradorRepository;

    @Autowired
    private PessoaAutorizadaRepository pessoaAutorizadaRepository;


    //CRUD Pessoa Autorizada

    //POST
    @Transactional
    public DadosConsultaPessoaAutorizadaDTO registrarPessoaAutorizada(DadosRegistrarPessoaAutorizadaDTO dados){

        var morador = moradorRepository.findById(dados.moradorIdMorador())
                .orElseThrow(() -> new EntityNotFoundException("Morador dono da autorização não encontrado."));

        var pessoaAutorizada = new PessoaAutorizada(dados, morador);
        pessoaAutorizadaRepository.save(pessoaAutorizada);
        return new DadosConsultaPessoaAutorizadaDTO(pessoaAutorizada);

    }

    //GET Lista Pessoas Autorizadas
    @Transactional(readOnly = true)
    public List<DadosConsultaPessoaAutorizadaDTO> listarTodasPessoasAutorizadas() {
        return pessoaAutorizadaRepository.findAll().stream()
                .map(DadosConsultaPessoaAutorizadaDTO::new)
                .toList();
    }

    //GET Pessoa Autorizada por ID
    @Transactional(readOnly = true)
    public DadosConsultaPessoaAutorizadaDTO buscarPessoaAutorizadaPorId(Long id) {
        PessoaAutorizada pessoaAutorizada = pessoaAutorizadaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa autorizada não encontrada"));
        return new DadosConsultaPessoaAutorizadaDTO(pessoaAutorizada);
    }

    //GET Lista Pessoas Autorizadas por Morador
    @Transactional(readOnly = true)
    public List<DadosConsultaPessoaAutorizadaDTO> buscarPessoaAutorizadaPorIdMorador(Long idMorador) {
        Usuario usuario = moradorRepository.findUsuarioByMoradorId(idMorador)
                .orElseThrow(() -> new EntityNotFoundException("Morador não encontrado"));

        if (usuario.getPessoa().getMorador() == null) {
            throw new EntityNotFoundException("O usuário informado não é um morador.");
        }

        List<PessoaAutorizada> pessoaAutorizadas = pessoaAutorizadaRepository.findByMoradorId(idMorador);

        return pessoaAutorizadas.stream()
                .map(DadosConsultaPessoaAutorizadaDTO::new)
                .toList();
    }

    //PUT Pessoa Autorizada
    @Transactional
    public DadosConsultaPessoaAutorizadaDTO editarPessoaAutorizada(DadosAtualizacaoPessoaAutorizadaDTO dados, Long idPessoaAutorizada){


        var pessoaAutorizada = pessoaAutorizadaRepository.findById(idPessoaAutorizada)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa Autorizada não encontrada!"));



        if(dados.cpf() != null){
            pessoaAutorizada.setCpf(dados.cpf());
        }

        if(dados.nome() != null){
            pessoaAutorizada.setNome(dados.nome());
        }


        // O Hibernate salva tudo (PessoaAutorizada) automaticamente ao final da transação.
        return new DadosConsultaPessoaAutorizadaDTO(pessoaAutorizada);
    }

    @Transactional
    public void desativarPessoaAutorizada(long id){

        PessoaAutorizada pessoaAutorizada = pessoaAutorizadaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pessoa não encontrada."));

        pessoaAutorizada.setAtivo(false);

        pessoaAutorizadaRepository.save(pessoaAutorizada);

    }

}
