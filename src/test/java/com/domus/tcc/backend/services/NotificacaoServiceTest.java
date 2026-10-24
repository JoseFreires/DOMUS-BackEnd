package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Notificacao;
import com.domus.tcc.backend.dto.response.DadosConsultaNotificacaoDTO;
import com.domus.tcc.backend.repository.NotificacaoRepository;
import com.domus.tcc.backend.security.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

    @Mock
    private NotificacaoRepository notificacaoRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificacaoService notificacaoService;

    @Test
    void persisteEEnviaNotificacaoAoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setUsername("morador@domus.com");
        when(notificacaoRepository.save(any(Notificacao.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        notificacaoService.notificar(usuario, "Novo aviso publicado: Manutenção");

        ArgumentCaptor<Notificacao> captor = ArgumentCaptor.forClass(Notificacao.class);
        verify(notificacaoRepository).save(captor.capture());
        assertEquals(usuario, captor.getValue().getUsuarioDestinatario());
        assertEquals("Novo aviso publicado: Manutenção", captor.getValue().getDescricao());
        verify(messagingTemplate).convertAndSendToUser(
                eq("morador@domus.com"),
                eq("/queue/notificacoes"),
                any(DadosConsultaNotificacaoDTO.class));
    }

    @Test
    void listaNotificacoesEmOrdemDeDataMaisRecente() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        Notificacao notificacao = new Notificacao(usuario, "Chamado concluído");
        when(notificacaoRepository.findByUsuarioDestinatario_IdOrderByDataHoraChegadaDesc(7L))
                .thenReturn(List.of(notificacao));

        List<DadosConsultaNotificacaoDTO> resultado = notificacaoService.listarDoUsuario(7L);

        assertEquals("Chamado concluído", resultado.get(0).descricao());
        assertEquals(notificacao.getDataHoraChegada(), resultado.get(0).dataHoraChegada());
    }
}
