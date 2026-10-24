package com.domus.tcc.backend.repository;

import com.domus.tcc.backend.domain.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findByUsuarioDestinatario_IdOrderByDataHoraChegadaDesc(Long usuarioId);
}
