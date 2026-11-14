package com.domus.tcc.backend.services;

import java.time.LocalDateTime;

import com.domus.tcc.backend.domain.ContaAdm;
import com.domus.tcc.backend.infra.aop.LogSistema;
import com.domus.tcc.backend.repository.LogSistemaRepository;
import com.domus.tcc.backend.security.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogSistemaService {

    private final LogSistemaRepository repository;

    public LogSistemaService(LogSistemaRepository repository) {
        this.repository = repository;
    }

    @Async("logExecutor")
    public void salvarPorUsuario(Usuario usuario, String metodo, String endpoint) {
        salvar(usuario, null, metodo, endpoint);
    }

    @Async("logExecutor")
    public void salvarResposta(Object principal, String metodo, String endpoint, int status) {
        HttpStatus httpStatus = HttpStatus.resolve(status);
        String descricaoStatus = httpStatus != null ? httpStatus.getReasonPhrase() : "Unknown Status";
        String acao = metodo + " - " + status + " " + descricaoStatus;

        Usuario usuario = principal instanceof Usuario usuarioPrincipal ? usuarioPrincipal : null;
        ContaAdm contaAdm = principal instanceof ContaAdm contaPrincipal ? contaPrincipal : null;
        salvar(usuario, contaAdm, acao, endpoint);
    }

    private void salvar(Usuario usuario, ContaAdm contaAdm, String acao, String endpoint) {
        LogSistema registro = new LogSistema();
        registro.setAcaoRealizada(acao);
        registro.setTabelaAlterada(endpoint);
        registro.setDataHora(LocalDateTime.now());
        registro.setUsuario(usuario);
        registro.setContaAdm(contaAdm);
        try {
            repository.save(registro);
        } catch (DataAccessException e) {
            log.error("Falha ao gravar log de sistema", e);
        }
    }
}