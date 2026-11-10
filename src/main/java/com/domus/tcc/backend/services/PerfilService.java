package com.domus.tcc.backend.services;

import java.time.LocalDateTime;

import com.domus.tcc.backend.domain.ContaAdm;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.domain.Porteiro;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoPerfilDTO;
import com.domus.tcc.backend.dto.response.DadosPerfilResponseDTO;
import com.domus.tcc.backend.repository.ContaAdmRepository;
import com.domus.tcc.backend.repository.PessoaRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PerfilService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ContaAdmRepository contaAdmRepository;
    @Autowired
    private PessoaRepository pessoaRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private FotoService fotoService;

    @Transactional(readOnly = true)
    public DadosPerfilResponseDTO consultarPerfil(Object principal) {
        if (principal instanceof Usuario usuarioPrincipal) {
            Usuario usuario = carregarUsuario(usuarioPrincipal);
            return montarPerfil(usuario);
        }

        if (principal instanceof ContaAdm contaAdmPrincipal) {
            ContaAdm contaAdm = contaAdmRepository.findById(contaAdmPrincipal.getIdContaAdm())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta administrativa não encontrada"));
            if (!contaAdm.isEnabled()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta inativa");
            }
            return new DadosPerfilResponseDTO.Administrador(
                    contaAdm.getIdContaAdm(),
                    contaAdm.getUsername(),
                    contaAdm.getNomeConta()
            );
        }

        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
    }

    @Transactional
    public AlteracaoPerfil alterarPerfil(Object principal, DadosAtualizacaoPerfilDTO dados, MultipartFile novaFoto) {
        Usuario usuario = carregarUsuario(principal);
        Pessoa pessoa = usuario.getPessoa();

        if (dados.nomeCompleto() != null) {
            pessoa.setNomeCompleto(dados.nomeCompleto());
        }

        if (dados.email() != null) {
            boolean emailAlterado = pessoa.getEmail() == null
                    || !pessoa.getEmail().equalsIgnoreCase(dados.email());
            boolean usernameAlterado = usuario.getUsername() == null
                    || !usuario.getUsername().equalsIgnoreCase(dados.email());

            if (emailAlterado && pessoaRepository.existsByEmailIgnoreCaseAndIdNot(dados.email(), pessoa.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
            }
            if (usernameAlterado && usuarioRepository.existsByUsernameIgnoreCaseAndIdNot(dados.email(), usuario.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já utilizado como login");
            }

            pessoa.setEmail(dados.email());
            usuario.setUsername(dados.email());
        }

        if (dados.telefone() != null) {
            pessoa.setTelefone(dados.telefone());
        }

        if (novaFoto != null && !novaFoto.isEmpty()) {
            String fotoAntiga = pessoa.getFotoPerfil();
            try {
                if (fotoAntiga != null && !fotoAntiga.isBlank()) {
                    fotoService.deletarFotoPessoa(fotoAntiga);
                }
                String novaFotoUrl = fotoService.uploadFotoPessoa(novaFoto);
                pessoa.setFotoPerfil(novaFotoUrl);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao atualizar a foto do perfil: " + e.getMessage(), e);
            }
        }

        pessoaRepository.save(pessoa);
        usuarioRepository.save(usuario);

        return new AlteracaoPerfil(montarPerfil(usuario), tokenService.gerarTokenUsuario(usuario));
    }

    @Transactional
    public void excluirPerfil(Object principal) {
        Usuario usuario = carregarUsuario(principal);
        Pessoa pessoa = usuario.getPessoa();

        if (!Boolean.TRUE.equals(pessoa.getAtivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta inativa");
        }

        pessoa.setAtivo(false);
        if (papel(usuario).equals("ROLE_MORADOR")) {
            Morador morador = pessoa.getMorador();
            if (morador == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dados de morador não encontrados");
            }
            morador.setDataSaida(LocalDateTime.now());
        }
        pessoaRepository.save(pessoa);
    }

    private Usuario carregarUsuario(Object principal) {
        if (!(principal instanceof Usuario usuarioPrincipal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta operação não é permitida para este perfil");
        }

        Usuario usuario = usuarioRepository.findByIdComDadosPerfil(usuarioPrincipal.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
        if (!usuario.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta inativa");
        }
        return usuario;
    }

    private DadosPerfilResponseDTO montarPerfil(Usuario usuario) {
        Pessoa pessoa = usuario.getPessoa();
        return switch (papel(usuario)) {
            case "ROLE_MORADOR" -> montarPerfilMorador(usuario, pessoa);
            case "ROLE_SINDICO" -> new DadosPerfilResponseDTO.Sindico(
                    usuario.getId(),
                    usuario.getUsername(),
                    pessoa.getNomeCompleto(),
                    pessoa.getCpf(),
                    pessoa.getEmail(),
                    pessoa.getTelefone(),
                    pessoa.getDataNascimento(),
                    pessoa.getFotoPerfil()
            );
            case "ROLE_PORTEIRO" -> montarPerfilPorteiro(usuario, pessoa);
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Papel sem perfil disponível");
        };
    }

    private DadosPerfilResponseDTO.Morador montarPerfilMorador(Usuario usuario, Pessoa pessoa) {
        Morador morador = pessoa.getMorador();
        if (morador == null || morador.getMoradia() == null || morador.getMoradia().getBloco() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dados de morador não encontrados");
        }
        return new DadosPerfilResponseDTO.Morador(
                usuario.getId(),
                morador.getId(),
                usuario.getUsername(),
                pessoa.getNomeCompleto(),
                pessoa.getCpf(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                pessoa.getDataNascimento(),
                pessoa.getFotoPerfil(),
                morador.getDataChegada(),
                morador.getMoradia().getId(),
                morador.getMoradia().getBloco().getId(),
                morador.getMoradia().getNumero(),
                morador.getMoradia().getBloco().getNome_torre()
        );
    }

    private DadosPerfilResponseDTO.Porteiro montarPerfilPorteiro(Usuario usuario, Pessoa pessoa) {
        Porteiro porteiro = pessoa.getPorteiro();
        if (porteiro == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Dados de porteiro não encontrados");
        }
        return new DadosPerfilResponseDTO.Porteiro(
                usuario.getId(),
                porteiro.getId(),
                usuario.getUsername(),
                pessoa.getNomeCompleto(),
                pessoa.getCpf(),
                pessoa.getEmail(),
                pessoa.getTelefone(),
                pessoa.getDataNascimento(),
                pessoa.getFotoPerfil(),
                porteiro.getTurno(),
                porteiro.getEmpresaResponsavel()
        );
    }

    private String papel(Usuario usuario) {
        return usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário sem papel definido"));
    }

    public record AlteracaoPerfil(DadosPerfilResponseDTO perfil, String tokenJwt) {
    }
}
