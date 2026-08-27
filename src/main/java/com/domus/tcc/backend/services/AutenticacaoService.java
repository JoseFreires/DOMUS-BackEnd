package com.domus.tcc.backend.services;

import com.domus.tcc.backend.repository.ContaAdmRepository;
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(repository.findByUsername(username) != null){
            return repository.findByUsername(username);
        } else {
            return repositoryContaAdm.findByUsername(username);
        }

    }


}