package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Notificacao;
import com.domus.tcc.backend.dto.response.DadosConsultaNotificacaoDTO;
import com.domus.tcc.backend.repository.NotificacaoRepository;
import com.domus.tcc.backend.security.Usuario;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificacaoService {

    private static final String DESTINO_NOTIFICACOES = "/queue/notificacoes";

    private final NotificacaoRepository notificacaoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.notificacaoRepository = notificacaoRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void notificar(Usuario destinatario, String descricao) {
        DadosConsultaNotificacaoDTO resposta = registrar(destinatario, descricao);
        messagingTemplate.convertAndSendToUser(
                destinatario.getUsername(),
                DESTINO_NOTIFICACOES,
                resposta);
    }

    @Transactional
    public DadosConsultaNotificacaoDTO registrar(Usuario destinatario, String descricao) {
        Notificacao notificacao = notificacaoRepository.save(new Notificacao(destinatario, descricao));
        return new DadosConsultaNotificacaoDTO(notificacao);
    }

    @Transactional(readOnly = true)
    public List<DadosConsultaNotificacaoDTO> listarDoUsuario(Long usuarioId) {
        return notificacaoRepository.findByUsuario_IdOrderByDataHoraDesc(usuarioId)
                .stream()
                .map(DadosConsultaNotificacaoDTO::new)
                .toList();
    }
}
