package com.domus.tcc.backend.services;

import com.domus.tcc.backend.repository.ContaAdmRepository;
import com.domus.tcc.backend.security.Usuario;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.domus.tcc.backend.repository.UsuarioRepository;


@Service
public class AutenticacaoService implements UserDetailsService{

    @Autowired
    UsuarioRepository repository;

    @Autowired
    ContaAdmRepository repositoryContaAdm;

    @Autowired
    private LogSistemaService logSistemaService;

    @Autowired
    private TokenService tokenService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(repository.findByUsername(username) != null){
            return repository.findByUsername(username);
        } else {
            return repositoryContaAdm.findByUsername(username);
        }

    }

    @Transactional
    public String aceitarTermosPolitica(Long usuarioId) {
        Usuario usuario = repository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        usuario.aceitarTermosPolitica();
        repository.save(usuario);

        logSistemaService.salvarPorUsuario(usuario, "PUT", "/api/auth/aceitar-termos");

        return tokenService.gerarTokenUsuario(usuario);
    }

}