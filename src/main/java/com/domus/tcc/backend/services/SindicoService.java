package com.domus.tcc.backend.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domus.tcc.backend.domain.Encomenda;
import com.domus.tcc.backend.domain.Moradia;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.domain.Porteiro;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoMoradorDTO;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoPorteiroDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarMoradorDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarPorteiroDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaBlocoDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaBlocoMoradiasDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaEncomendaDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradiaDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradiaMoradoresDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradorDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaMoradorEncomendasDTO;
import com.domus.tcc.backend.dto.response.DadosConsultaPorteiroDTO;
import com.domus.tcc.backend.repository.BlocoRepository;
import com.domus.tcc.backend.repository.EncomendaRepository;
import com.domus.tcc.backend.repository.MoradiaRepository;
import com.domus.tcc.backend.repository.MoradorRepository;
import com.domus.tcc.backend.repository.PapelRepository;
import com.domus.tcc.backend.repository.PessoaRepository;
import com.domus.tcc.backend.repository.PorteiroRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Papel;
import com.domus.tcc.backend.security.Usuario;

import jakarta.persistence.EntityNotFoundException;

@Service
public class SindicoService {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MoradorRepository moradorRepository;

    @Autowired
    private EncomendaRepository encomendaRepository;

    @Autowired
    private PorteiroRepository porteiroRepository;

    @Autowired
    private PapelRepository papelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BlocoRepository blocoRepository;

    @Autowired
    private MoradiaRepository moradiaRepository;


    //CRUD MORADOR

    //POST Morador
    @Transactional
    public DadosConsultaMoradorDTO registrarMorador(DadosRegistrarMoradorDTO dados) {

        //Buscando a moradia para associar (como tinhaos falado nas reuniões moradia etc já vai ter cadastro)
        Moradia moradia = moradiaRepository.findById(dados.idMoradia())
                .orElseThrow(() -> new EntityNotFoundException("Moradia/Apartamento não encontrado no sistema."));

        // Agora o papel é fixo então ele busca no banco
        Papel papelMorador = papelRepository.findByNomePapel("ROLE_MORADOR")
                .orElseThrow(() -> new EntityNotFoundException("O papel ROLE_MORADOR não está configurado no banco."));

        //Instancia de pessoa
        Pessoa pessoa = new Pessoa(dados.pessoa());

        //Instancia de Morador (Não é mais um papel)
        Morador morador = new Morador(dados, moradia);

        // Inserindo composição
        morador.setPessoa(pessoa);
        pessoa.setMorador(morador);
        pessoaRepository.save(pessoa);

        //Senha com a lógica do Rian de criptografia
        String senhaCodificada = passwordEncoder.encode(dados.usuario().senha());
        Usuario usuario = new Usuario(dados.usuario(), senhaCodificada);

        //Colocando navegabilidade (como no diagrama)
        usuario.setPessoa(pessoa);

        //Inserindo Role Morador
        usuario.setPapel(papelMorador);

        //Salva Usuario
        usuarioRepository.save(usuario);

        // retorna o usuario completo
        return new DadosConsultaMoradorDTO(usuario);
    }


    //GET List Morador
    @Transactional(readOnly = true)
    public List<DadosConsultaMoradorDTO> listarTodasMoradores() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getPessoa().getMorador() != null &&
                        Boolean.TRUE.equals(usuario.getPessoa().getAtivo()))
                .map(DadosConsultaMoradorDTO::new)
                .toList();
    }

    //GET Morador por ID
    @Transactional(readOnly = true)
    public DadosConsultaMoradorDTO buscarMoradorPorId(Long idMorador) {
        Usuario usuario = moradorRepository.findUsuarioByMoradorId(idMorador)
                .orElseThrow(() -> new EntityNotFoundException("Morador não encontrado"));

        if (usuario.getPessoa().getMorador() == null) {
            throw new EntityNotFoundException("O usuário informado não é um morador.");
        }

        return new DadosConsultaMoradorDTO(usuario);
    }

    //GET Encomendas do Morador
    @Transactional(readOnly = true)
    public DadosConsultaMoradorEncomendasDTO buscarEncomendasMorador(Long idMorador) {
        Usuario usuario = moradorRepository.findUsuarioByMoradorId(idMorador)
                .orElseThrow(() -> new EntityNotFoundException("Morador não encontrado"));


        if (usuario.getPessoa().getMorador() == null) {
            throw new EntityNotFoundException("O usuário informado não é um morador.");
        }

        List<Encomenda> encomendas = encomendaRepository.findByMoradorId(idMorador);

        return new DadosConsultaMoradorEncomendasDTO(encomendas.stream()
                                                        .map(DadosConsultaEncomendaDTO::new)
                                                        .toList());
    }



    //PUT Morador
    @Transactional
    public DadosConsultaMoradorDTO editarMorador(Long idMorador, DadosAtualizacaoMoradorDTO dados){


        Usuario usuario = moradorRepository.findUsuarioByMoradorId(idMorador)
                .orElseThrow(() -> new EntityNotFoundException("Morador não encontrado!"));


        var pessoa = usuario.getPessoa();
        var morador = pessoa.getMorador();


        if(dados.moradiaIdMoradia() != null){
            var novaMoradia = moradiaRepository.findById(dados.moradiaIdMoradia())
                    .orElseThrow(() -> new EntityNotFoundException("Nova moradia não encontrada!"));
            morador.setMoradia(novaMoradia);
        }

        if(dados.nomeCompleto() != null){
            pessoa.setNomeCompleto(dados.nomeCompleto());
        }

        if(dados.dataNascimento() != null){
            pessoa.setDataNascimento(dados.dataNascimento());
        }

        if(dados.telefone() != null){
            pessoa.setTelefone(dados.telefone());
        }

        // O Hibernate salva tudo (Pessoa e Morador) automaticamente ao final da transação.
        return new DadosConsultaMoradorDTO(usuario);
    }

    //"DELETE" Morador
    @Transactional
    public void desativarMorador(Long idMorador){

        Usuario usuario = moradorRepository.findUsuarioByMoradorId(idMorador)
                .orElseThrow(() -> new EntityNotFoundException("Morador não encontrado!"));

        var pessoaMorador = usuario.getPessoa();
        pessoaMorador.setAtivo(false);
        pessoaMorador.getMorador().setDataSaida(LocalDateTime.now());

        pessoaRepository.save(pessoaMorador);
    }





    //CRUD PORTEIRO

    //POST Porteiro
    @Transactional
    public DadosConsultaPorteiroDTO registrarPorteiro(DadosRegistrarPorteiroDTO dados){

        //Busca Papel Porteiro
        Papel papelPorteiro = papelRepository.findByNomePapel("ROLE_PORTEIRO")
                .orElseThrow(() -> new EntityNotFoundException("O papel ROLE_PORTEIRO não está configurado no banco."));

        Pessoa pessoa = new Pessoa(dados.pessoa());

        Porteiro porteiro = new Porteiro(dados);

        porteiro.setPessoa(pessoa);
        pessoa.setPorteiro(porteiro);
        pessoaRepository.save(pessoa);

        String senhaCodificada = passwordEncoder.encode(dados.usuario().senha());
        Usuario usuario = new Usuario(dados.usuario(), senhaCodificada);

        //Colocando navegabilidade
        usuario.setPessoa(pessoa);

        //Inserindo Role porteiro
        usuario.setPapel(papelPorteiro);

        //Salva Usuario
        usuarioRepository.save(usuario);

        return new DadosConsultaPorteiroDTO(usuario);
    }


    //GET List Porteiro
    @Transactional(readOnly = true)
    public List<DadosConsultaPorteiroDTO> listarTodosPorteiros() {
        return usuarioRepository.findAll().stream()
                .filter(usuario -> usuario.getPessoa().getPorteiro() != null &&
                        Boolean.TRUE.equals(usuario.getPessoa().getAtivo()))
                .map(DadosConsultaPorteiroDTO::new)
                .toList();
    }

    //GET Porteiro por Id
    public DadosConsultaPorteiroDTO buscarPorteiroPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Porteiro não encontrado"));

        if (usuario.getPessoa() == null || usuario.getPessoa().getPorteiro() == null) {
            throw new EntityNotFoundException("O usuário informado não é um porteiro.");
        }

        return new DadosConsultaPorteiroDTO(usuario);
    }


    //PUT Porteiro
    @Transactional
    public DadosConsultaPorteiroDTO editarPorteiro(Long idPorteiro, DadosAtualizacaoPorteiroDTO dados){


        Usuario usuario = porteiroRepository.findUsuarioByPorteiroId(idPorteiro)
                .orElseThrow(() -> new EntityNotFoundException("Porteiro não encontrado!"));

        var pessoa = usuario.getPessoa();
        var porteiro = pessoa.getPorteiro();

        if(dados.empresaResponsavel() != null){
            porteiro.setEmpresaResponsavel(dados.empresaResponsavel());
        }

        if (dados.turno() != null) {
            porteiro.setTurno(dados.turno());
        }

        if(dados.nomeCompleto() != null){
            pessoa.setNomeCompleto(dados.nomeCompleto());
        }

        if(dados.dataNascimento() != null){
            pessoa.setDataNascimento(dados.dataNascimento());
        }

        if(dados.telefone() != null){
            pessoa.setTelefone(dados.telefone());
        }

        // O Hibernate salva tudo (Pessoa e Porteiro) automaticamente ao final da transação.
        return new DadosConsultaPorteiroDTO(usuario);
    }


    //"DELETE" Porteiro
    @Transactional
    public void desativarPorteiro(Long id){

        Usuario UsuarioPorteiro = porteiroRepository.findUsuarioByPorteiroId(id)
                .orElseThrow(() -> new EntityNotFoundException("Porteiro não encontrado."));

        var pessoaPorteiro = UsuarioPorteiro.getPessoa();
        pessoaPorteiro.setAtivo(false);

        pessoaRepository.save(pessoaPorteiro);
    }



    //CONSULTA CONDOMINIAL

    //GET lista Moradias
    @Transactional(readOnly = true)
    public List<DadosConsultaMoradiaDTO> listarTodasMoradias() {
        return moradiaRepository.findAll().stream()
                .map(DadosConsultaMoradiaDTO::new)
                .toList();
    }

    //GET Moradia por ID
    @Transactional(readOnly = true)
    public DadosConsultaMoradiaDTO buscarMoradiaPorId(Long id) {
        var moradia = moradiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Moradia não encontrada"));
        return new DadosConsultaMoradiaDTO(moradia);
    }

    //GET Moradores por ID moradia
    @Transactional(readOnly = true)
    public DadosConsultaMoradiaMoradoresDTO listarMoradoresPorMoradiaId(Long id) {
        var moradia = moradiaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Moradia não encontrada"));
        return new DadosConsultaMoradiaMoradoresDTO(moradia);
    }


    //GET lista Bloco
    @Transactional(readOnly = true)
    public List<DadosConsultaBlocoDTO> listarTodosBlocos() {
        return blocoRepository.findAll().stream()
                .map(DadosConsultaBlocoDTO::new)
                .toList();
    }

    //GET Bloco por ID
    @Transactional(readOnly = true)
    public DadosConsultaBlocoDTO buscarBlocoPorId(Long id) {
        var bloco = blocoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bloco não encontrado"));
        return new DadosConsultaBlocoDTO(bloco);
    }

    //GET Moradias por ID bloco
    @Transactional(readOnly = true)
    public DadosConsultaBlocoMoradiasDTO listarMoradiasPorBlocoId(Long id) {
        var bloco = blocoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Moradia não encontrada"));
        return new DadosConsultaBlocoMoradiasDTO(bloco);
    }
}