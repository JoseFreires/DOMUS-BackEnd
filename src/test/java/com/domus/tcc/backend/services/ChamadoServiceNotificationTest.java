package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.domain.enums.TipoChamado;
import com.domus.tcc.backend.security.Papel;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoChamadoDTO;
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
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
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
    void deveNotificarSindicoQuandoMoradorAbreChamado() throws Exception {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Usuario sindico = criarUsuario(2L, "sindico@domus.com", 20L, "Síndico");
        Morador morador = new Morador();
        morador.setId(30L);
        morador.setPessoa(moradorUsuario.getPessoa());

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(moradorUsuario));
        when(moradorRepository.findByPessoa_Id(10L)).thenReturn(Optional.of(morador));
        when(usuarioRepository.findByPapel_IdPapel(4L)).thenReturn(Optional.of(sindico));
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "chamado.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(fotoService.uploadFotoChamado(arquivo)).thenReturn("http://minio/chamados/chamado.jpg");
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> {
            Chamado chamado = invocation.getArgument(0);
            chamado.setId(100L);
            return chamado;
        });

        chamadoService.registrarChamado(
                new DadosRegistrarChamado("Barulho", "Barulho após as 22h", TipoChamado.RECLAMACAO),
                moradorUsuario,
                arquivo);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("sindico@domus.com"),
                org.mockito.ArgumentMatchers.eq(DESTINO_NOTIFICACOES),
                payload.capture());

        DadosNotificacaoChamadoDTO notificacao =
                assertInstanceOf(DadosNotificacaoChamadoDTO.class, payload.getValue());
        assertEquals("NOVO_CHAMADO", notificacao.tipo());
        assertEquals(100L, notificacao.chamado().idChamado());
        assertEquals("http://minio/chamados/chamado.jpg", notificacao.chamado().imagem());
        verify(fotoService).uploadFotoChamado(arquivo);
        verify(notificacaoService).registrar(sindico, "Morador abriu o chamado #100: Barulho");
        ArgumentCaptor<EmailNotificacaoEvent> eventoEmail =
                ArgumentCaptor.forClass(EmailNotificacaoEvent.class);
        verify(applicationEventPublisher).publishEvent(eventoEmail.capture());
        assertEquals("Morador abriu o chamado #100: Barulho", eventoEmail.getValue().mensagem());
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
                new DadosAtualizarStatusChamadoDTO(StatusChamado.CONCLUIDO, null),
                criarUsuarioComPapel(2L, "sindico@domus.com", 20L, "Síndico", "ROLE_SINDICO"));

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("morador@domus.com"),
                org.mockito.ArgumentMatchers.eq(DESTINO_NOTIFICACOES),
                payload.capture());

        DadosNotificacaoChamadoDTO notificacao =
                assertInstanceOf(DadosNotificacaoChamadoDTO.class, payload.getValue());
        assertEquals("STATUS_ATUALIZADO", notificacao.tipo());
        assertEquals(StatusChamado.CONCLUIDO, notificacao.chamado().statusChamado());
        assertEquals(
                "O status do chamado #100 (\"Barulho\") foi atualizado para CONCLUIDO.",
                notificacao.mensagem());
        verify(notificacaoService).registrar(
                moradorUsuario,
                "O status do chamado #100 (\"Barulho\") foi atualizado para CONCLUIDO.");
        verify(applicationEventPublisher).publishEvent(any(EmailNotificacaoEvent.class));
    }

    @Test
    void deveImpedirEdicaoQuandoChamadoNaoEstaAberto() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.TRATANDO);

        autenticarMorador(moradorUsuario, morador);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.editarChamado(
                        100L,
                        new DadosAtualizacaoChamadoDTO(
                                "Novo título", "Nova descrição", TipoChamado.SUGESTAO),
                        moradorUsuario,
                        null));

        assertEquals(409, exception.getStatusCode().value());
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    @Test
    void deveSubstituirImagemDoChamadoAbertoUsandoBucketChamados() throws Exception {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.ABERTO);
        chamado.setImagem("http://minio/chamados/imagem-antiga.jpg");
        MockMultipartFile arquivo = new MockMultipartFile(
                "arquivo", "imagem-nova.jpg", "image/jpeg", new byte[]{4, 5, 6});

        autenticarMorador(moradorUsuario, morador);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));
        when(fotoService.uploadFotoChamado(arquivo))
                .thenReturn("http://minio/chamados/imagem-nova.jpg");
        when(chamadoRepository.save(chamado)).thenReturn(chamado);

        var resultado = chamadoService.editarChamado(
                100L,
                new DadosAtualizacaoChamadoDTO(
                        "Barulho", "Barulho após as 22h", TipoChamado.RECLAMACAO),
                moradorUsuario,
                arquivo);

        assertEquals("http://minio/chamados/imagem-nova.jpg", resultado.imagem());
        InOrder ordem = inOrder(fotoService);
        ordem.verify(fotoService).uploadFotoChamado(arquivo);
        ordem.verify(fotoService).deletarFotoChamado("http://minio/chamados/imagem-antiga.jpg");
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void deveCancelarChamadoAbertoSemApagaLo() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.ABERTO);

        autenticarMorador(moradorUsuario, morador);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        chamadoService.cancelarChamado(100L, moradorUsuario);

        assertEquals(StatusChamado.CANCELADO, chamado.getStatusChamado());
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void deveImpedirMoradorDeAlterarChamadoDeOutroMorador() {
        Usuario outroMorador = criarUsuario(3L, "outro@domus.com", 30L, "Outro morador");
        Morador donoChamado = criarMorador(
                criarUsuario(1L, "morador@domus.com", 10L, "Morador"));
        Morador moradorAutenticado = criarMorador(outroMorador);
        moradorAutenticado.setId(31L);
        Chamado chamado = criarChamado(donoChamado, StatusChamado.ABERTO);

        autenticarMorador(outroMorador, moradorAutenticado);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.cancelarChamado(100L, outroMorador));

        assertEquals(403, exception.getStatusCode().value());
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    @Test
    void deveReabrirChamadoConcluidoComDescricaoComoJustificativa() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        adicionarPapel(moradorUsuario, "ROLE_MORADOR");
        Usuario sindico = criarUsuario(2L, "sindico@domus.com", 20L, "Síndico");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.CONCLUIDO);
        chamado.setSindico(sindico);
        chamado.setDataHoraFinalizado(LocalDateTime.now());

        autenticarMorador(moradorUsuario, morador);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(any(Chamado.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var resultado = chamadoService.atualizarStatusChamado(
                100L,
                new DadosAtualizarStatusChamadoDTO(
                        StatusChamado.ABERTO, "O problema continua acontecendo."),
                moradorUsuario);

        assertEquals(StatusChamado.ABERTO, resultado.statusChamado());
        assertEquals("O problema continua acontecendo.", resultado.descricao());
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void deveExigirDescricaoParaReabrirChamado() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        adicionarPapel(moradorUsuario, "ROLE_MORADOR");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.CONCLUIDO);

        autenticarMorador(moradorUsuario, morador);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.atualizarStatusChamado(
                        100L,
                        new DadosAtualizarStatusChamadoDTO(StatusChamado.ABERTO, "  "),
                        moradorUsuario));

        assertEquals(400, exception.getStatusCode().value());
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    private Morador criarMorador(Usuario usuario) {
        Morador morador = new Morador();
        morador.setId(30L);
        morador.setPessoa(usuario.getPessoa());
        return morador;
    }

    private Chamado criarChamado(Morador morador, StatusChamado status) {
        Chamado chamado = new Chamado();
        chamado.setId(100L);
        chamado.setTitulo("Barulho");
        chamado.setDescricao("Barulho após as 22h");
        chamado.setTipoChamado(TipoChamado.RECLAMACAO);
        chamado.setStatusChamado(status);
        chamado.setDataHoraAbertura(LocalDateTime.now());
        chamado.setMorador(morador);
        chamado.setSindico(criarUsuario(2L, "sindico@domus.com", 20L, "Síndico"));
        return chamado;
    }

    private void autenticarMorador(Usuario usuario, Morador morador) {
        when(usuarioRepository.findById(usuario.getId())).thenReturn(Optional.of(usuario));
        when(moradorRepository.findByPessoa_Id(usuario.getPessoa().getId()))
                .thenReturn(Optional.of(morador));
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

    private Usuario criarUsuarioComPapel(
            Long id, String username, Long pessoaId, String nome, String nomePapel) {
        Usuario usuario = criarUsuario(id, username, pessoaId, nome);
        adicionarPapel(usuario, nomePapel);
        return usuario;
    }

    private void adicionarPapel(Usuario usuario, String nomePapel) {
        Papel papel = new Papel();
        papel.setNomePapel(nomePapel);
        usuario.setPapel(papel);
    }
}
