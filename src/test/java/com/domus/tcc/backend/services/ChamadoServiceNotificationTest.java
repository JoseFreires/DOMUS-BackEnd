package com.domus.tcc.backend.services;

import com.domus.tcc.backend.domain.Chamado;
import com.domus.tcc.backend.domain.Morador;
import com.domus.tcc.backend.domain.Pessoa;
import com.domus.tcc.backend.domain.enums.StatusChamado;
import com.domus.tcc.backend.domain.enums.TipoChamado;
import com.domus.tcc.backend.dto.request.DadosAtualizacaoChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosAtualizarStatusChamadoDTO;
import com.domus.tcc.backend.dto.request.DadosRegistrarChamado;
import com.domus.tcc.backend.dto.response.DadosNotificacaoChamadoDTO;
import com.domus.tcc.backend.exception.StatusInvalidoException;
import com.domus.tcc.backend.repository.ChamadoRepository;
import com.domus.tcc.backend.repository.MoradorRepository;
import com.domus.tcc.backend.repository.UsuarioRepository;
import com.domus.tcc.backend.security.Papel;
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
import static org.junit.jupiter.api.Assertions.assertNull;
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
                new DadosRegistrarChamado(
                        "Barulho",
                        "Abri um chamado anterior que foi concluído (100), mas o problema não foi resolvido.",
                        TipoChamado.RECLAMACAO,
                        null),
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
        assertEquals(
                "Abri um chamado anterior que foi concluído (100), mas o problema não foi resolvido.",
                notificacao.chamado().descricaoInicial());
        assertEquals("http://minio/chamados/chamado.jpg", notificacao.chamado().imagem());
        verify(fotoService).uploadFotoChamado(arquivo);
        verify(notificacaoService).registrar(sindico, "Morador abriu o chamado #100: Barulho");
        ArgumentCaptor<EmailNotificacaoEvent> eventoEmail =
                ArgumentCaptor.forClass(EmailNotificacaoEvent.class);
        verify(applicationEventPublisher).publishEvent(eventoEmail.capture());
        assertEquals("Morador abriu o chamado #100: Barulho", eventoEmail.getValue().mensagem());
    }

    @Test
    void deveNotificarMoradorQuandoPorteiroConcluiChamadoComResolucao() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Usuario porteiro = criarUsuarioComPapel(2L, "porteiro@domus.com", 20L, "Porteiro", "ROLE_PORTEIRO");
        Morador morador = new Morador();
        morador.setId(30L);
        morador.setPessoa(moradorUsuario.getPessoa());

        Chamado chamado = new Chamado();
        chamado.setId(100L);
        chamado.setTitulo("Barulho");
        chamado.setDescricaoInicial("Barulho após as 22h");
        chamado.setTipoChamado(TipoChamado.RECLAMACAO);
        chamado.setStatusChamado(StatusChamado.TRATANDO);
        chamado.setDataHoraAbertura(LocalDateTime.now());
        chamado.setMorador(morador);
        chamado.setSindico(porteiro);

        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(chamado)).thenReturn(chamado);
        when(usuarioRepository.findByPessoa_Id(10L)).thenReturn(Optional.of(moradorUsuario));

        var resultado = chamadoService.atualizarStatusChamado(
                100L,
                new DadosAtualizarStatusChamadoDTO(StatusChamado.CONCLUIDO, "Ruído resolvido."),
                porteiro);

        assertEquals(StatusChamado.CONCLUIDO, resultado.statusChamado());
        assertEquals("Ruído resolvido.", resultado.resolucaoChamado());
        assertEquals("Barulho após as 22h", resultado.descricaoInicial());
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

        StatusInvalidoException exception = assertThrows(
                StatusInvalidoException.class,
                () -> chamadoService.editarChamado(
                        100L,
                        new DadosAtualizacaoChamadoDTO(
                                "Novo título", "Nova descrição", TipoChamado.SUGESTAO, null),
                        moradorUsuario,
                        null));

        assertEquals("Somente chamados com status ABERTO podem ser alterados ou cancelados",
                exception.getMessage());
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
                        "Barulho", "Barulho após as 22h", TipoChamado.RECLAMACAO, null),
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
        Usuario moradorUsuario = criarUsuarioComPapel(
                1L, "morador@domus.com", 10L, "Morador", "ROLE_MORADOR");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.ABERTO);

        autenticarMorador(moradorUsuario, morador);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(chamado)).thenReturn(chamado);

        chamadoService.atualizarStatusChamado(
                100L,
                new DadosAtualizarStatusChamadoDTO(StatusChamado.CANCELADO, null),
                moradorUsuario);

        assertEquals(StatusChamado.CANCELADO, chamado.getStatusChamado());
        verify(chamadoRepository).save(chamado);
    }

    @Test
    void deveImpedirMoradorDeAlterarChamadoDeOutroMorador() {
        Usuario outroMorador = criarUsuarioComPapel(
                3L, "outro@domus.com", 30L, "Outro morador", "ROLE_MORADOR");
        Morador donoChamado = criarMorador(
                criarUsuario(1L, "morador@domus.com", 10L, "Morador"));
        Morador moradorAutenticado = criarMorador(outroMorador);
        moradorAutenticado.setId(31L);
        Chamado chamado = criarChamado(donoChamado, StatusChamado.ABERTO);

        autenticarMorador(outroMorador, moradorAutenticado);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.atualizarStatusChamado(
                        100L,
                        new DadosAtualizarStatusChamadoDTO(StatusChamado.CANCELADO, null),
                        outroMorador));

        assertEquals(403, exception.getStatusCode().value());
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    @Test
    void deveListarSomenteChamadosDoMoradorAutenticado() {
        Usuario moradorUsuario = criarUsuarioComPapel(
                1L, "morador@domus.com", 10L, "Morador", "ROLE_MORADOR");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.ABERTO);

        autenticarMorador(moradorUsuario, morador);
        when(chamadoRepository.findAllByStatusChamadoAndMorador_Id(StatusChamado.ABERTO, 30L))
                .thenReturn(java.util.List.of(chamado));

        var resultado = chamadoService.listarChamados(StatusChamado.ABERTO, moradorUsuario);

        assertEquals(1, resultado.size());
        assertEquals(100L, resultado.get(0).idChamado());
        verify(chamadoRepository).findAllByStatusChamadoAndMorador_Id(StatusChamado.ABERTO, 30L);
        verify(chamadoRepository, never()).findAll();
    }

    @Test
    void deveImpedirMoradorDeConsultarChamadoDeOutroMorador() {
        Usuario outroMorador = criarUsuarioComPapel(
                3L, "outro@domus.com", 30L, "Outro morador", "ROLE_MORADOR");
        Morador moradorAutenticado = criarMorador(outroMorador);
        moradorAutenticado.setId(31L);
        Morador donoChamado = criarMorador(
                criarUsuario(1L, "morador@domus.com", 10L, "Morador"));
        Chamado chamado = criarChamado(donoChamado, StatusChamado.ABERTO);

        autenticarMorador(outroMorador, moradorAutenticado);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.buscarChamadoPorId(100L, outroMorador));

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void deveImpedirMoradorDeEditarChamadoDeOutroMorador() {
        Usuario outroMorador = criarUsuarioComPapel(
                3L, "outro@domus.com", 30L, "Outro morador", "ROLE_MORADOR");
        Morador moradorAutenticado = criarMorador(outroMorador);
        moradorAutenticado.setId(31L);
        Morador donoChamado = criarMorador(
                criarUsuario(1L, "morador@domus.com", 10L, "Morador"));
        Chamado chamado = criarChamado(donoChamado, StatusChamado.ABERTO);

        autenticarMorador(outroMorador, moradorAutenticado);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.editarChamado(
                        100L,
                        new DadosAtualizacaoChamadoDTO(
                                "Novo título", "Nova descrição", TipoChamado.SUGESTAO, null),
                        outroMorador,
                        null));

        assertEquals(403, exception.getStatusCode().value());
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    @Test
    void deveImpedirSindicoDeCancelarChamado() {
        Usuario sindico = criarUsuarioComPapel(2L, "sindico@domus.com", 20L, "Síndico", "ROLE_SINDICO");
        Chamado chamado = criarChamado(
                criarMorador(criarUsuario(1L, "morador@domus.com", 10L, "Morador")),
                StatusChamado.ABERTO);
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.atualizarStatusChamado(
                        100L, new DadosAtualizarStatusChamadoDTO(StatusChamado.CANCELADO, null), sindico));

        assertEquals(403, exception.getStatusCode().value());
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    @Test
    void deveImpedirConclusaoAntesDeTratar() {
        Chamado chamado = criarChamado(criarMorador(
                criarUsuario(1L, "morador@domus.com", 10L, "Morador")), StatusChamado.ABERTO);
        Usuario sindico = criarUsuarioComPapel(2L, "sindico@domus.com", 20L, "Síndico", "ROLE_SINDICO");
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        assertThrows(StatusInvalidoException.class, () -> chamadoService.atualizarStatusChamado(
                100L, new DadosAtualizarStatusChamadoDTO(StatusChamado.CONCLUIDO, "Resolvido"), sindico));
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    @Test
    void deveTratarChamadoComPorteiroSemAlterarOutrosCampos() {
        Usuario moradorUsuario = criarUsuario(1L, "morador@domus.com", 10L, "Morador");
        Morador morador = criarMorador(moradorUsuario);
        Chamado chamado = criarChamado(morador, StatusChamado.ABERTO);
        Usuario porteiro = criarUsuarioComPapel(3L, "porteiro@domus.com", 30L, "Porteiro", "ROLE_PORTEIRO");

        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));
        when(chamadoRepository.save(chamado)).thenReturn(chamado);
        when(usuarioRepository.findByPessoa_Id(10L)).thenReturn(Optional.of(moradorUsuario));

        chamadoService.atualizarStatusChamado(
                100L, new DadosAtualizarStatusChamadoDTO(StatusChamado.TRATANDO, null), porteiro);

        assertEquals(StatusChamado.TRATANDO, chamado.getStatusChamado());
        assertEquals("Barulho após as 22h", chamado.getDescricaoInicial());
        assertNull(chamado.getResolucaoChamado());
        assertNull(chamado.getDataHoraFinalizado());
    }

    @Test
    void deveExigirResolucaoApenasAoConcluir() {
        Chamado chamado = criarChamado(criarMorador(
                criarUsuario(1L, "morador@domus.com", 10L, "Morador")), StatusChamado.TRATANDO);
        Usuario porteiro = criarUsuarioComPapel(3L, "porteiro@domus.com", 30L, "Porteiro", "ROLE_PORTEIRO");
        when(chamadoRepository.findById(100L)).thenReturn(Optional.of(chamado));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> chamadoService.atualizarStatusChamado(
                        100L, new DadosAtualizarStatusChamadoDTO(StatusChamado.CONCLUIDO, "  "), porteiro));

        assertEquals(400, exception.getStatusCode().value());
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    private Usuario criarUsuarioComPapel(
            Long id, String username, Long pessoaId, String nome, String nomePapel) {
        Usuario usuario = criarUsuario(id, username, pessoaId, nome);
        Papel papel = new Papel();
        papel.setNomePapel(nomePapel);
        usuario.setPapel(papel);
        return usuario;
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
        chamado.setDescricaoInicial("Barulho após as 22h");
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

}
