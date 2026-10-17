package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.domain.enums.TipoChamado;
import com.domus.tcc.backend.dto.request.DadosAtualizarStatusChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarChamado;
import com.domus.tcc.backend.dto.response.DadosNotificacaoChamadoDTO;
import com.domus.tcc.backend.repository.ChamadoRepository;
import com.domus.tcc.backend.repository.MoradorRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Usuario;
import com.domus.tcc.backend.util.EmailNotificacaoEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceNotificationTest {

    private static final String DESTINO_NOTIFICACOES = "/queue/notificacoes";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MoradorRepository moradorRepository;

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private FotoService fotoService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private NotificacaoService notificacaoService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ChamadoService chamadoService;

    @Test
    void deveNotificarSindicoQuandoMoradorAbreChamado() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Usuario sindico = criarUsuario(2L, "sindico@domus.com", 20L, "Síndico");
        Morador morador = new Morador();
        morador.setId(30L);
        morador.setPessoa(moradorUsuario.getPessoa());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(moradorUsuario));
        when(moradorRepository.findByPessoa_Id(10L)).thenReturn(Optional.of(morador));
        when(usuarioRepository.findByPapel_IdPapel(4L)).thenReturn(Optional.of(sindico));
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> {
            Chamado chamado = invocation.getArgument(0);
            chamado.setId(100L);
            return chamado;
        });

        chamadoService.registrarChamado(
                new DadosRegistrarChamado("Barulho", "Barulho após as 22h", TipoChamado.RECLAMACAO),
                moradorUsuario,
                null);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("sindico@domus.com"),
                org.mockito.ArgumentMatchers.eq(DESTINO_NOTIFICACOES),
                payload.capture());

        DadosNotificacaoChamadoDTO notificacao =
                assertInstanceOf(DadosNotificacaoChamadoDTO.class, payload.getValue());
        assertEquals("NOVO_CHAMADO", notificacao.tipo());
        assertEquals(100L, notificacao.chamado().idChamado());
                verify(notificacaoService).registrar(sindico, "Morador abriu um chamado: Barulho");
                verify(applicationEventPublisher).publishEvent(any(EmailNotificacaoEvent.class));
    }

    @Test
    void deveNotificarMoradorQuandoSindicoAtualizaStatus() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Usuario sindico = criarUsuario(2L, "sindico@domus.com", 20L, "Síndico");
        Morador morador = new Morador();
        morador.setId(30L);
        morador.setPessoa(moradorUsuario.getPessoa());

        Chamado chamado = new Chamado();
        chamado.setId(100L);
        chamado.setTitulo("Barulho");
        chamado.setDescricao("Barulho após as 22h");
        chamado.setTipoChamado(TipoChamado.RECLAMACAO);
        chamado.setStatusChamado(StatusChamado.ABERTO);
        chamado.setDataHoraAbertura(LocalDateTime.now());
        chamado.setMorador(morador);
        chamado.setSindico(sindico);

        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));
        when(usuarioRepository.findByPessoa_Id(10L)).thenReturn(Optional.of(moradorUsuario));

        chamadoService.atualizarStatusChamado(
                100L,
                new DadosAtualizarStatusChamadoDTO(StatusChamado.CONCLUIDO));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("morador@domus.com"),
                org.mockito.ArgumentMatchers.eq(DESTINO_NOTIFICACOES),
                payload.capture());

        DadosNotificacaoChamadoDTO notificacao =
                assertInstanceOf(DadosNotificacaoChamadoDTO.class, payload.getValue());
        assertEquals("STATUS_ATUALIZADO", notificacao.tipo());
        assertEquals(StatusChamado.CONCLUIDO, notificacao.chamado().statusChamado());
        verify(notificacaoService).registrar(
                moradorUsuario,
                "O status do chamado \"Barulho\" foi atualizado para CONCLUIDO.");
        verify(applicationEventPublisher).publishEvent(any(EmailNotificacaoEvent.class));
    }

    private Usuario criarUsuario(Long id, String username, Long pessoaId, String nome) {
        Pessoa pessoa = new Pessoa();
        pessoa.setId(pessoaId);
        pessoa.setNomeCompleto(nome);

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setPessoa(pessoa);
        return usuario;
    }
}
