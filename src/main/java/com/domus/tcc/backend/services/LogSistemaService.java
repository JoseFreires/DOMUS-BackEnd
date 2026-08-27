package com.domus.tcc.backend.services;

import java.time.LocalDateTime;

import com.domus.tcc.backend.domain.ContaAdm;
import org.springframework.stereotype.Service;

import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.infra.aop.LogSistema;
import com.domus.tcc.backend.repository.LogSistemaRepository;

@Service
public class LogSistemaService {

    private final LogSistemaRepository repository;

    public LogSistemaService(LogSistemaRepository repository) {
        this.repository = repository;
    }

    public void salvarPorUsuario(Usuario usuario, String metodo, String endpoint) {
        LogSistema log = new LogSistema();
        log.setAcaoRealizada(metodo);
        log.setTabelaAlterada(endpoint);
        log.setDataHora(LocalDateTime.now());
        log.setUsuario(usuario);
        log.setContaAdm(null);
        repository.save(log);
    }

    public void salvarPorContaAdm(ContaAdm contaAdm, String metodo, String endpoint) {
        LogSistema log = new LogSistema();
        log.setAcaoRealizada(metodo);
        log.setTabelaAlterada(endpoint);
        log.setDataHora(LocalDateTime.now());
        log.setUsuario(null);
        log.setContaAdm(contaAdm);
        repository.save(log);
    }
}