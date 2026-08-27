package com.domus.tcc.backend.services;

import com.domus.tcc.backend.dto.*;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoPessoaDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarPessoaDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarSindicoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaPessoaDTO;
import com.domus.tcc.backend.security.Papel;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.repository.PapelRepository;
import com.domus.tcc.backend.repository.PessoaRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdmService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public DadosConsultaPessoaDTO registrarSindico(DadosRegistrarSindicoDTO dados) {

        Papel papelSindico = papelRepository.findByNomePapel("ROLE_SINDICO")
                .orElseThrow(() -> new EntityNotFoundException("O papel ROLE_SINDICO não está configurada no banco."));

        Pessoa pessoa = new Pessoa(dados.pessoa());
        pessoaRepository.save(pessoa);

        Usuario usuario = new Usuario(dados.login(), passwordEncoder.encode(dados.login().senha()));

        usuario.setPessoa(pessoa);
        usuario.setPapel(papelSindico);

        usuarioRepository.save(usuario);

        return new DadosConsultaPessoaDTO(usuario);
    }

    //GET List Sindico
    @Transactional(readOnly = true)
    public List<DadosConsultaPessoaDTO> listarTodosSindicos() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getPapel() != null &&
                        usuario.getPapel().getNomePapel().equals("ROLE_SINDICO") &&
                        Boolean.TRUE.equals(usuario.getPessoa().getAtivo()))
                .map(DadosConsultaPessoaDTO::new)
                .toList();
    }

    //GET Sindico por Id
    @Transactional(readOnly = true)
    public DadosConsultaPessoaDTO buscarSindicoPorId(Long id) {
        var sindico = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sindico não encontrado"));
        return new DadosConsultaPessoaDTO(sindico);
    }

    //PUT Sindico
    @Transactional
    public DadosConsultaPessoaDTO editarSindico(Long idUsuario, DadosAtualizacaoPessoaDTO dados){


        var usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado!"));

        if (usuario.getPapel().equals("ROLE_SINDICO")){
            var pessoaSindico = usuario.getPessoa();

            if(dados.email() != null){
                pessoaSindico.setEmail(dados.email());
            }

            if (dados.dataNascimento() != null) {
                pessoaSindico.setDataNascimento(dados.dataNascimento());
            }

            if(dados.nomeCompleto() != null){
                pessoaSindico.setNomeCompleto(dados.nomeCompleto());
            }

            if(dados.telefone() != null){
                pessoaSindico.setTelefone(dados.telefone());
            }

            // O Hibernate salva tudo (PessoaSindico) automaticamente ao final da transação.
            return new DadosConsultaPessoaDTO(usuario);
        } else{
            System.out.println("Vish não é sindico");
        }

        return null;
    }


    //"DELETE" Sindico
    @Transactional
    public void desativarSindico(Long id){

        var usuarioSindico = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sindico não encontrado."));

        var pessoaSindico = usuarioSindico.getPessoa();
        pessoaSindico.setAtivo(false);

        pessoaRepository.save(pessoaSindico);
    }

}
