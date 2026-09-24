package com.domus.tcc.backend.services;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.domus.tcc.backend.dto.request.DadosNovaSenhaDTO;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.util.RedefinicaoSenhaSolicitadaEvent;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RedefinicaoSenhaService {

    private static final String FRONTEND_RESET_URL = "http://localhost:3000/redefinir-senha?token=";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TokenService tokenService;

    public RedefinicaoSenhaService(UsuarioRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder,
                                   ApplicationEventPublisher applicationEventPublisher,
                                   TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationEventPublisher = applicationEventPublisher;
        this.tokenService = tokenService;
    }

    @Transactional
    public void solicitarRedefinicao(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("O e-mail informado é inválido.");
        }

        String emailNormalizado = email.trim();

        Usuario usuario = usuarioRepository.findByPessoaEmailIgnoreCase(emailNormalizado)
                .or(() -> usuarioRepository.findByUsernameIgnoreCase(emailNormalizado))
                .orElse(null);

        if (usuario == null) {
            return;
        }

        String identificadorUsuario = usuario.getUsername();
        String token = tokenService.gerarTokenRedefinicaoSenha(identificadorUsuario);

        String nomeUsuario = usuario.getPessoa() != null && usuario.getPessoa().getNomeCompleto() != null
                ? usuario.getPessoa().getNomeCompleto()
                : "Usuário";

        String emailDestino = usuario.getPessoa() != null && usuario.getPessoa().getEmail() != null
                ? usuario.getPessoa().getEmail()
                : usuario.getUsername();

        applicationEventPublisher.publishEvent(new RedefinicaoSenhaSolicitadaEvent(
                emailDestino,
                nomeUsuario,
                "Domus",
                FRONTEND_RESET_URL + token
        ));
    }

    @Transactional
    public void redefinirSenha(String token, DadosNovaSenhaDTO dados) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token de redefinição inválido.");
        }

        if (dados == null) {
            throw new IllegalArgumentException("Dados da nova senha inválidos.");
        }

        String identificadorUsuario = tokenService.validarTokenRedefinicaoSenha(token.trim());

        Usuario usuario = usuarioRepository.findByUsernameIgnoreCase(identificadorUsuario)
                .or(() -> usuarioRepository.findByPessoaEmailIgnoreCase(identificadorUsuario))
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado para este token."));

        validarSenhaNova(dados, usuario);

        usuario.setSenha(passwordEncoder.encode(dados.novaSenha()));
        usuarioRepository.save(usuario);
    }

    private void validarSenhaNova(DadosNovaSenhaDTO dados, Usuario usuario) {
        if (dados.novaSenha() == null || dados.confirmarSenha() == null) {
            throw new IllegalArgumentException("A senha e a confirmação são obrigatórias.");
        }

        if (!dados.novaSenha().equals(dados.confirmarSenha())) {
            throw new IllegalArgumentException("As senhas informadas não conferem.");
        }

        if (dados.novaSenha().length() < 8) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 8 caracteres.");
        }

        if (!dados.novaSenha().matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra maiúscula.");
        }

        if (!dados.novaSenha().matches(".*[a-z].*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra minúscula.");
        }

        if (!dados.novaSenha().matches(".*\\d.*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos um número.");
        }

        if (passwordEncoder.matches(dados.novaSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("A nova senha deve ser diferente da senha atual.");
        }
    }
}
